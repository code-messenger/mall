package cool.yunlong.mall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import com.rabbitmq.client.Channel;
import cool.yunlong.mall.model.enums.ProcessStatus;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.model.payment.PaymentInfo;
import cool.yunlong.mall.mq.constant.MqConst;
import cool.yunlong.mall.mq.service.RabbitService;
import cool.yunlong.mall.order.service.OrderService;
import cool.yunlong.mall.payment.client.PaymentFeignClient;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/30 20:41
 */
@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

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
            //  判断订单id 是否存在！
            if (orderId != null) {
                //  根据订单Id 查询订单对象
                OrderInfo orderInfo = orderService.getById(orderId);
                //  判断
                if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())) {
                    //  关闭过期订单！ 还需要关闭对应的 paymentInfo ，还有alipay.
                    //  orderService.execExpiredOrder(orderId);
                    //  查询paymentInfo 是否存在！
                    PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                    //  判断 用户点击了扫码支付
                    if (paymentInfo != null && "UNPAID".equals(paymentInfo.getPaymentStatus())) {

                        //  查看是否有交易记录！
                        Boolean flag = paymentFeignClient.checkPayment(orderId);
                        //  判断
                        if (flag) {
                            //  flag = true , 有交易记录
                            //  调用关闭接口！ 扫码未支付这样才能关闭成功！
                            Boolean result = paymentFeignClient.closePay(orderId);
                            //  判断
                            if (result) {
                                //  result = true; 关闭成功！未付款！需要关闭orderInfo， paymentInfo，Alipay
                                orderService.execExpiredOrder(orderId, "2");
                            } //  result = false; 表示付款！
                            //  说明已经付款了！ 正常付款成功都会走异步通知！

                        } else {
                            //  没有交易记录，不需要关闭支付！  需要关闭orderInfo， paymentInfo
                            orderService.execExpiredOrder(orderId, "2");
                        }

                    } else {
                        //  只关闭订单orderInfo！
                        orderService.execExpiredOrder(orderId, "1");
                    }
                }
            }

        } catch (Exception e) {
            //  写入日志...
            e.printStackTrace();
        }
        //  手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 扣减库存成功，更新订单状态
     *
     * @param msgJson 消息内容
     * @throws IOException 异常
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void updateOrderStatus(String msgJson, Message message, Channel channel) throws IOException {
        if (!StringUtils.isEmpty(msgJson)) {
            Map<String, Object> map = JSON.parseObject(msgJson, Map.class);
            String orderId = (String) map.get("orderId");
            String status = (String) map.get("status");
            if ("DEDUCTED".equals(status)) {
                // 减库存成功！ 修改订单状态为已支付
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
            } else {
        /*
            减库存失败！远程调用其他仓库查看是否有库存！
            true:   orderService.sendOrderStatus(orderId); orderService.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
            false:  1.  补货  | 2.   人工客服。
         */
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
