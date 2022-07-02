package cool.yunlong.mall.order.client;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.order.client.impl.OrderFeignDegradeClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yunlong
 * @since 2022/6/28 22:42
 */
@FeignClient(value = "service-order", fallback = OrderFeignDegradeClient.class)
public interface OrderFeignClient {

    @GetMapping("/api/order/auth/trade")
    Result trade();

    /**
     * 根据订单id获取订单信息
     *
     * @param orderId 订单id
     * @return 订单信息
     */
    @GetMapping("/api/order//inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable Long orderId);
}
