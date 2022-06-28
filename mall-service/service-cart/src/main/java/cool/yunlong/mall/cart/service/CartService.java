package cool.yunlong.mall.cart.service;

import cool.yunlong.mall.model.cart.CartInfo;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 15:11
 */
public interface CartService {

    /**
     * 添加购物车
     *
     * @param skuId     sku 编号
     * @param userId    用户 id
     * @param skuNumber sku 下单数量
     */
    void addToCart(Long skuId, String userId, Integer skuNumber);

    /**
     * 获取购物车列表
     *
     * @param userId     用户 id
     * @param userTempId 临时用户 id
     * @return 购物车列表
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     * 删除购物车
     *
     * @param skuId  sku 编号
     * @param userId 用户 id
     */
    void deleteCart(Long skuId, String userId);

    /**
     * 选中状态
     *
     * @param userId    用户 id
     * @param isChecked 是否选中
     * @param skuId     sku 编号
     */
    void checkCart(Long skuId, String userId, Integer isChecked);

    /**
     * 获取选中状态为 1 的购物车商品集合
     *
     * @param userId 用户 id
     * @return 购物车列表
     */
    List<CartInfo> getCartCheckedList(String userId);
}
