package cool.yunlong.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.common.cache.MallCache;
import cool.yunlong.mall.common.constant.RedisConst;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.mq.constant.MqConst;
import cool.yunlong.mall.mq.service.RabbitService;
import cool.yunlong.mall.product.mapper.*;
import cool.yunlong.mall.product.service.SkuManageService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yunlong
 * @since 2022/6/14 11:18
 */
@Service
public class SkuManageServiceImpl implements SkuManageService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitService rabbitService;

    /**
     * 保存sku信息
     * sku_info: 库存单元表      sku_sale_attr_value: 库存单元销售属性表      sku_image: 库存单元图片表      sku_attr_value  库存单元属性表
     * <p>
     * 四张表的关联关系:
     * sku_info: id -> sku_sale_attr_value: sku_id -> sku_image: sku_id -> sku_attr_value: sku_id
     *
     * @param skuInfo sku信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 1. 保存skuInfo
        skuInfoMapper.insert(skuInfo);
        // 2. 保存sku关联信息
        saveSkuList(skuInfo);
        // 保存到布隆过滤器中
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        // 把skuId添加进去
        bloomFilter.add(skuInfo.getId());
    }

    private void saveSkuList(SkuInfo skuInfo) {
        // 保存Sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }
        // 保存sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }
        // 保存sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }
    }

    /**
     * 分页查询sku列表
     *
     * @param pageInfo 分页信息
     * @param skuInfo  sku信息
     * @return 分页结果
     */
    @Override
    public IPage<SkuInfo> getSkuInfoPage(Page<SkuInfo> pageInfo, SkuInfo skuInfo) {
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", skuInfo.getCategory3Id());
        queryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(pageInfo, queryWrapper);
    }

    /**
     * 更新sku信息
     *
     * @param skuInfo sku信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSkuInfo(SkuInfo skuInfo) {
        // 1. 先删除sku图片信息
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuInfo.getId());
        skuImageMapper.delete(queryWrapper);

        // 2. 删除sku销售属性值信息
        QueryWrapper<SkuSaleAttrValue> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("sku_id", skuInfo.getId());
        skuSaleAttrValueMapper.delete(queryWrapper1);

        // 3. 删除sku属性值信息
        QueryWrapper<SkuAttrValue> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("sku_id", skuInfo.getId());
        skuAttrValueMapper.delete(queryWrapper2);

        // 4. 保存sku信息
        skuInfoMapper.updateById(skuInfo);

        // 5. 保存sku关联信息
        saveSkuList(skuInfo);
    }

    @MallCache(prefix = "skuInfo:")
    public SkuInfo getSkuInfoRedis(Long skuId) {
//        return getSkuInfoByRedisLock(skuId);
//        return getSkuInfoByRedissonLock(skuId);
        return getDbSkuInfo(skuId);
    }

    private SkuInfo getSkuInfoByRedissonLock(Long skuId) {
        SkuInfo skuInfo;
        try {
            // 定义缓存的key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            // 获取缓存中的数据，看是否有数据存在
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 判断是否存在
            if (skuInfo == null) {
                // 说明缓存中没有数据   加分布式锁
                // 设置分布式锁的key
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
                // 上锁   redisson
                RLock lock = redissonClient.getLock(lockKey);
                // 参数一： 最大等待时间 参数二： 过期时间 参数三： 时间单位
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (result) {
                    try {
                        // 表示获取到锁 执行业务
                        skuInfo = getDbSkuInfo(skuId);
                        if (skuInfo == null) {
                            // 防止缓存穿透 ，临时缓存null
                            SkuInfo skuInfo1 = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        // 数据库有数据，直接放入缓存
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
                // 没有获取到锁
                try {
                    // 睡眠 1 s
                    Thread.sleep(1000);
                    // 自旋
                    return getSkuInfoRedis(skuId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // 缓存有数据，直接返回
                return skuInfo;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 数据库兜底操作
        return getDbSkuInfo(skuId);
    }

    /**
     * 原生redis做分布式锁
     *
     * @param skuId sku编号
     * @return sku基本信息
     */
    private SkuInfo getSkuInfoByRedisLock(Long skuId) {
        SkuInfo skuInfo;
        try {
            // 缓存存储数据：key-value
            // 定义key sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            // 获取里面的数据？ redis 有五种数据类型 那么我们存储商品详情 使用哪种数据类型？
            // 获取缓存数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 如果从缓存中获取的数据是空
            if (skuInfo == null) {
                // 直接获取数据库中的数据，可能会造成缓存击穿。所以在这个位置，应该添加锁。
                // 第一种：redis ，第二种：redisson
                // 定义锁的key sku:skuId:lock  set k1 v1 px 10000 nx
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                // 定义锁的值
                String uuid = UUID.randomUUID().toString().replace("-", "");
                // 上锁
                Boolean isExist = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (isExist) {
                    // 执行成功的话，则上锁。
                    System.out.println("获取到分布式锁！");
                    // 真正获取数据库中的数据 {数据库中到底有没有这个数据 = 防止缓存穿透}
                    skuInfo = getDbSkuInfo(skuId);
                    // 从数据库中获取的数据就是空
                    if (skuInfo == null) {
                        // 为了避免缓存穿透 应该给空的对象放入缓存
                        SkuInfo skuInfo1 = new SkuInfo(); //对象的地址
                        redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfo1;
                    }
                    // 查询数据库的时候，有值
                    redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    // 解锁：使用lua 脚本解锁
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    // 设置lua脚本返回的数据类型
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    // 设置lua脚本返回类型为Long
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptText(script);
                    // 删除key 所对应的 value
                    redisTemplate.execute(redisScript, Collections.singletonList(lockKey), uuid);
                    return skuInfo;
                } else {
                    // 其他线程等待
                    Thread.sleep(1000);
                    return getSkuInfoRedis(skuId);
                }
            } else {
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 为了防止缓存宕机：从数据库中获取数据
        return getDbSkuInfo(skuId);
    }


    /**
     * 商品上架
     *
     * @param skuId sku编号
     */
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        // 发送商品上架消息
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_GOODS, MqConst.ROUTING_GOODS_UPPER, skuId);
    }

    /**
     * 商品下架
     *
     * @param skuId sku编号
     */
    @Override
    public void offSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        // 发送商品下架消息
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_GOODS, MqConst.ROUTING_GOODS_LOWER, skuId);
    }

    /**
     * 根据skuId查询商品详情信息
     *
     * @param skuId sku编号
     * @return 商品详情信息
     */
    @Override
    public SkuInfo getAllSkuInfo(Long skuId) {
        // 查询sku基本信息
        SkuInfo skuInfo = getDbSkuInfo(skuId);

        // 查询sku销售属性值信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = getSkuSaleAttrValueList(skuId);
        // 设置sku销售属性值信息
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        // 查询sku属性值信息
        List<SkuAttrValue> skuAttrValueList = getSkuAttrValueList(skuId);
        // 设置sku属性值信息
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }


    /**
     * 获取sku基本信息
     *
     * @param skuId sku编号
     * @return sku基本信息
     */
    private SkuInfo getDbSkuInfo(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo != null) {
            QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
            skuImageQueryWrapper.eq("sku_id", skuId);
            List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    /**
     * 根据skuId查询sku属性值信息
     *
     * @param skuId sku编号
     * @return 属性值信息
     */
    public List<SkuAttrValue> getSkuAttrValueList(Long skuId) {
        QueryWrapper<SkuAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.selectList(queryWrapper);

        // 查询属性名称信息
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                // 设置attrName
                BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectOne(new QueryWrapper<BaseAttrInfo>().eq("id", skuAttrValue.getAttrId()));
                skuAttrValue.setAttrName(baseAttrInfo.getAttrName());
                // 设置valueName
                BaseAttrValue baseAttrValue = baseAttrValueMapper.selectOne(new QueryWrapper<BaseAttrValue>().eq("id", skuAttrValue.getValueId()));
                skuAttrValue.setValueName(baseAttrValue.getValueName());
            });
        }
        return skuAttrValueList;
    }

    /**
     * 根据skuId查询sku销售属性值信息
     *
     * @param skuId sku编号
     * @return 销售属性值信息
     */
    public List<SkuSaleAttrValue> getSkuSaleAttrValueList(Long skuId) {
        QueryWrapper<SkuSaleAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectList(queryWrapper);

        // 查询销售属性名称信息
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                //查询销售属性值表
                SpuSaleAttrValue spuSaleAttrValue = spuSaleAttrValueMapper.selectById(skuSaleAttrValue.getSaleAttrValueId());
                //设置销售属性值名
                skuSaleAttrValue.setSaleAttrValueName(spuSaleAttrValue.getSaleAttrValueName());
                //设置销售属性名
                skuSaleAttrValue.setSaleAttrName(spuSaleAttrValue.getSaleAttrName());
                //设置销售属性ID
                skuSaleAttrValue.setBaseSaleAttrId(spuSaleAttrValue.getBaseSaleAttrId());
            });
        }
        return skuSaleAttrValueList;
    }

    /**
     * 根据三级分类id查询分类信息
     *
     * @param category3Id 三级分类id
     * @return 分类列表
     */
    @MallCache(prefix = "categoryView:")
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        // select * from base_category_view where category3_id = ?
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 根据skuId查询sku的价格
     *
     * @param skuId sku编号
     * @return sku的价格
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        // 设置缓存key
        String lockKey = "price:" + skuId;
        // 上锁
        RLock lock = redissonClient.getLock(lockKey);

        lock.lock();

        // select price from sku_info where id = ? and is_deleted = 0
        try {
            QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("price").eq("id", skuId);
            SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);
            return skuInfo == null ? new BigDecimal(0) : skuInfo.getPrice();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return new BigDecimal(0);
    }

    /**
     * 根据spuId、skuId 查询销售属性列表
     *
     * @param skuId sku编号
     * @param spuId spu编号
     * @return 销售属性列表
     */
    @MallCache(prefix = "spuSaleAttr")
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * 根据spuId查询sku的销售属性组合
     *
     * @param spuId spu编号
     * @return 销售属性组合
     */
    @MallCache(prefix = "skuValueIds:")
    @Override
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
        // key = 125|123 ,value = 37
        List<Map<Object, Object>> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        if (mapList != null && mapList.size() > 0) {
            // 循环遍历
            for (Map<Object, Object> skuMap : mapList) {
                // key = 125|123 ,value = 37
                map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
            }
        }
        return map;
    }

    /**
     * 根据skuId获取规格参数 --> 平台属性数据
     *
     * @param skuId sku编号
     * @return 平台属性列表
     */
    @MallCache(prefix = "baseAttrInfo:")
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }

}

