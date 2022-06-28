package cool.yunlong.mall.cart.service.impl;

import cool.yunlong.mall.cart.service.CartService;
import cool.yunlong.mall.common.constant.RedisConst;
import cool.yunlong.mall.common.util.DateUtil;
import cool.yunlong.mall.model.cart.CartInfo;
import cool.yunlong.mall.model.product.SkuInfo;
import cool.yunlong.mall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yunlong
 * @since 2022/6/28 15:12
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Qualifier("cool.yunlong.mall.product.client.ProductFeignClient")
    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 添加购物车
     *
     * @param skuId     sku 编号
     * @param userId    用户 id
     * @param skuNumber sku 下单数量
     */
    @Override
    public void addToCart(Long skuId, String userId, Integer skuNumber) {
        /*
            1.  购物车中没有该商品的时候，直接将商品添加到购物车中
            2.  购物车中有该商品，则数量相加
            3.  每次添加购物车的时候，都是默认选中状态
            4.  每次添加的时候，覆盖上一次的更新时间

             购物车采用的数据类型是什么?
             Hash  hset key field value     hget key field
             key = user:userId:cart     谁的购物车
             field = skuId              商品Id
             value = cartInfo           购物项数据
         */

        //  定义购物车的key = user:userId:cart
        String cartKey = getCartKey(userId);
        //  获取购物车中是否有该商品
        CartInfo cartInfoExist = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
        //  判断
        if (cartInfoExist != null) {
            //  购物车中有这个商品
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNumber);
            //  默认状态  0: 表示没有被选中，1：表示选中
            if (cartInfoExist.getIsChecked() == 0) {
                cartInfoExist.setIsChecked(1);
            }
            //  赋值商品的最新价格：sku_info.price
            cartInfoExist.setSkuPrice(this.productFeignClient.getSkuPrice(skuId));
            //  覆盖这条记录的修改时间
            cartInfoExist.setUpdateTime(new Date());
        } else {
            //  当购物车中不存在这个商品的时候
            //  远程调用
            SkuInfo skuInfo = this.productFeignClient.getSkuInfo(skuId);
            //  创建对象
            cartInfoExist = new CartInfo();
            cartInfoExist.setUserId(userId);
            cartInfoExist.setSkuId(skuId);
            cartInfoExist.setCartPrice(this.productFeignClient.getSkuPrice(skuId));
            cartInfoExist.setSkuNum(skuNumber);
            cartInfoExist.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoExist.setSkuName(skuInfo.getSkuName());
            cartInfoExist.setSkuPrice(this.productFeignClient.getSkuPrice(skuId));
            cartInfoExist.setCreateTime(new Date());
            cartInfoExist.setUpdateTime(new Date());
        }
        //  将数据存储到缓存
        this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfoExist);
    }


    /**
     * 删除购物车
     *
     * @param skuId  sku 编号
     * @param userId 用户 id
     */
    @Override
    public void deleteCart(Long skuId, String userId) {
        //  获取购物车的key
        String cartKey = this.getCartKey(userId);
        //  删除购物车
        this.redisTemplate.opsForHash().delete(cartKey, skuId.toString());
    }

    /**
     * 选中状态
     *
     * @param userId    用户 id
     * @param isChecked 是否选中
     * @param skuId     sku 编号
     */
    @Override
    public void checkCart(Long skuId, String userId, Integer isChecked) {
        //  获取到购物车的key
        String cartKey = this.getCartKey(userId);
        //  获取到修改的商品
        CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo != null) {
            //  赋值修改状态
            cartInfo.setIsChecked(isChecked);
            //  将修改之后的数据在保存到缓存
            this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);
        }
    }

    /**
     * 获取选中状态为 1 的购物车商品集合
     *
     * @param userId 用户 id
     * @return 购物车列表
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //  获取购物车的 key
        String cartKey = this.getCartKey(userId);

        //  获取购物车列表
        List<CartInfo> cartInfoList = this.redisTemplate.opsForHash().values(cartKey);

        //  过滤选中状态为 1 的购物车列表并返回
        return cartInfoList.stream().filter(cartInfo -> cartInfo.getIsChecked() == 1).collect(Collectors.toList());
    }

    /**
     * 获取购物车列表
     *
     * @param userId     用户 id
     * @param userTempId 临时用户 id
     * @return 购物车列表
     */
    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        //  先声明一个集合来存储未登录购物车数据
        List<CartInfo> noLoginCartInfoList = new ArrayList<>();

        //  临时用户 Id 不为空
        if (!StringUtils.isEmpty(userTempId)) {
            //  先获取购物车的key
            String cartKey = this.getCartKey(userTempId);

            //  获取临时的购物车集合数据  hvals key;
            noLoginCartInfoList = this.redisTemplate.opsForHash().values(cartKey);
        }
        //  判断 userId 为空，同时未登录集合数据不为空！
        if (StringUtils.isEmpty(userId) && !CollectionUtils.isEmpty(noLoginCartInfoList)) {
            //  查看购物车列表的时候，应该按照购物车的更新时间进行排序。
            //  Comparator 自定义比较器
            noLoginCartInfoList.sort((c1, c2) -> DateUtil.truncatedCompareTo(c2.getUpdateTime(), c1.getUpdateTime(), Calendar.SECOND));
            return noLoginCartInfoList;
        }

        //  声明一个登录购物车集合；
        List<CartInfo> loginCartInfoList = new ArrayList<>();
        //  判断
        if (!StringUtils.isEmpty(userId)) {
            //  说明用户已经登录了
            //  先获取购物车的key
            String cartKey = this.getCartKey(userId);
            //  获取登录时的购物车集合数据  hvals key;
            //  loginCartInfoList = this.redisTemplate.opsForHash().values(cartKey);
            //  方式一：双重for 合并的时候，需要根据skuId 是否相等作为合并条件
            //  方式二：登录的购物车集合中，是否包含未登录的skuId
            //  BoundHashOperations<H, HK, HV>
            BoundHashOperations<String, String, CartInfo> boundHashOperations = this.redisTemplate.boundHashOps(cartKey);
            if (!CollectionUtils.isEmpty(noLoginCartInfoList)) {
                //  未登录购物车进行遍历
                noLoginCartInfoList.forEach(noLoginCartInfo -> {
                    //  判断  21,22,23,24
                    if (Boolean.TRUE.equals(boundHashOperations.hasKey(noLoginCartInfo.getSkuId().toString()))) {
                        //  商品的数量相加
                        //  先获取到登录的数据
                        CartInfo loginCartInfo = boundHashOperations.get(noLoginCartInfo.getSkuId().toString());
                        if (loginCartInfo != null) {
                            loginCartInfo.setSkuNum(loginCartInfo.getSkuNum() + noLoginCartInfo.getSkuNum());
                            //  修改更新时间
                            loginCartInfo.setUpdateTime(new Date());
                            //  为了保证价格的实时性
                            loginCartInfo.setSkuPrice(this.productFeignClient.getSkuPrice(loginCartInfo.getSkuId()));

                            //  有选中状态的问题:
                            if (noLoginCartInfo.getIsChecked() == 1) {
                                //  设置选中状态
                                if (loginCartInfo.getIsChecked() == 0) {
                                    loginCartInfo.setIsChecked(1);
                                }
                            }
                            //  保存数据到缓存：
                            boundHashOperations.put(noLoginCartInfo.getSkuId().toString(), loginCartInfo);
                        }
                    } else {
                        //  判断选中：
                        if (noLoginCartInfo.getIsChecked() == 1) {
                            //  直接写入到登录的购物车中  25
                            //  将原来的未登录userId 设置为登录的userId 111 ---> 1
                            noLoginCartInfo.setUserId(userId);
                            noLoginCartInfo.setCreateTime(new Date());
                            noLoginCartInfo.setUpdateTime(new Date());
                            // 写入缓存
                            this.redisTemplate.boundHashOps(cartKey).put(noLoginCartInfo.getSkuId().toString(), noLoginCartInfo);
                        }
                    }
                });

                //  删除未登录购物车数据.
                this.redisTemplate.delete(this.getCartKey(userTempId));
            }

            //  登录购物车中有数据了，要查询到最新的合并结果 21,22,23,24,25
            loginCartInfoList = boundHashOperations.values();
            //  判断当前集合为空。
            if (CollectionUtils.isEmpty(loginCartInfoList)) {
                return new ArrayList<>();
            }
            //  按照更新时间排序
            loginCartInfoList.sort((c1, c2) -> DateUtil.truncatedCompareTo(c2.getUpdateTime(), c1.getUpdateTime(), Calendar.SECOND));
        }

        //  返回购物车集合数据
        return loginCartInfoList;
    }

    /**
     * 根据 userId 设置缓存key
     *
     * @param userId 用户 id
     * @return 缓存key
     */
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}