package cool.yunlong.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.mapper.*;
import cool.yunlong.mall.product.service.SkuManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    }

    private void saveSkuList(SkuInfo skuInfo) {
        // 1. 保存sku图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }

        // 2. 保存sku销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }

        // 3. 保存sku属性值
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
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
    }

    /**
     * 根据skuId查询商品详情信息
     *
     * @param skuId sku编号
     * @return 商品详情信息
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        // 查询skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        // 查询sku图片信息
        List<SkuImage> skuImageList = getSkuImageList(skuId);
        // 设置sku图片信息
        skuInfo.setSkuImageList(skuImageList);

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
                BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectOne(
                        new QueryWrapper<BaseAttrInfo>().eq("id", skuAttrValue.getAttrId())
                );
                skuAttrValue.setAttrName(baseAttrInfo.getAttrName());
                // 设置valueName
                BaseAttrValue baseAttrValue = baseAttrValueMapper.selectOne(
                        new QueryWrapper<BaseAttrValue>().eq("id", skuAttrValue.getValueId())
                );
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
                // 查询saleAttrValueName
                SpuSaleAttrValue spuSaleAttrValue = spuSaleAttrValueMapper.selectById(skuSaleAttrValue.getSaleAttrValueId());
                skuSaleAttrValue.setSaleAttrValueName(spuSaleAttrValue.getSaleAttrValueName());
                spuSaleAttrValue.setBaseSaleAttrId(spuSaleAttrValue.getBaseSaleAttrId());
            });
        }
        return skuSaleAttrValueList;
    }

    /**
     * 根据skuId查询sku图片信息
     *
     * @param skuId sku编号
     * @return sku图片信息
     */
    public List<SkuImage> getSkuImageList(Long skuId) {
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        return skuImageMapper.selectList(queryWrapper);
    }

    /**
     * 根据三级分类id查询分类列表
     *
     * @param category3Id 三级分类id
     * @return 分类列表
     */
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
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
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo == null ? new BigDecimal(0) : skuInfo.getPrice();
    }

    /**
     * 根据spuId、skuId 查询销售属性列表
     *
     * @param skuId sku编号
     * @param spuId spu编号
     * @return 销售属性列表
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * 根据spuId查询sku的销售属性值列表
     *
     * @param spuId spu编号
     * @return 销售属性值列表
     */
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
     * 根据skuId查询平台属性列表
     *
     * @param skuId sku编号
     * @return 平台属性列表
     */
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }

}

