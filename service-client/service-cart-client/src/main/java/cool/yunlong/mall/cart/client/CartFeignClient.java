package cool.yunlong.mall.cart.client;

import cool.yunlong.mall.cart.client.impl.CartFeignDegradeClient;
import cool.yunlong.mall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 22:06
 */
@FeignClient(value = "service-cart", fallback = CartFeignDegradeClient.class)
public interface CartFeignClient {

    /**
     * 获取选中状态为 1 的购物车商品集合
     *
     * @param userId 用户 id
     * @return 购物车商品集合
     */
    @GetMapping("/api/cart/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable String userId);
}
