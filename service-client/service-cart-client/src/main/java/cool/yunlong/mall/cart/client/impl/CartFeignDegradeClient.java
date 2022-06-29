package cool.yunlong.mall.cart.client.impl;

import cool.yunlong.mall.cart.client.CartFeignClient;
import cool.yunlong.mall.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 22:07
 */
@Component
public class CartFeignDegradeClient implements CartFeignClient {
    /**
     * 获取选中状态为 1 的购物车商品集合
     *
     * @param userId 用户 id
     * @return 购物车商品集合
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        return null;
    }
}
