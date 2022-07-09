package cool.yunlong.mall.order.client.impl;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

/**
 * @author yunlong
 * @since 2022/6/28 22:43
 */
@Component
public class OrderFeignDegradeClient implements OrderFeignClient {
    @Override
    public Result trade() {
        return null;
    }

    /**
     * 根据订单id获取订单信息
     *
     * @param orderId 订单id
     * @return 订单信息
     */
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }

    /**
     * 提交秒杀订单
     *
     * @param orderInfo 订单信息
     * @return 订单id
     */
    @Override
    public Long submitOrder(OrderInfo orderInfo) {
        return null;
    }
}
