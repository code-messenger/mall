package cool.yunlong.mall.order.receiver;

import com.rabbitmq.client.Channel;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.mq.constant.MqConst;
import cool.yunlong.mall.order.service.OrderService;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yunlong
 * @since 2022/6/30 20:41
 */
@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;

    /**
     * 监听取消订单消息
     *
     * @param orderId 订单id
     * @param message 消息
     * @param channel 信道
     */
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel) {
        try {
            //  判断当前订单 id 是否为空
            if (orderId != null) {
                // 查询订单信息
                OrderInfo orderInfo = orderService.getById(orderId);
                // 判断订单支付状态、进度状态
                if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus())
                        && "UNPAID".equals(orderInfo.getProcessStatus())) {
                    //  关闭订单
                    orderService.execExpiredOrder(orderId);
                }
            }
        } catch (Exception e) {
            //  消息没有正常被消费者处理： 记录日志后续跟踪处理! 可以将消息内容存到mysql中，等待后续处理
            e.printStackTrace();
        }
        //  手动确认消息 如果不确认，有可能会到消息残留。
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
