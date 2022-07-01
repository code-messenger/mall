package cool.yunlong.mall.list.receiver;

import com.rabbitmq.client.Channel;
import cool.yunlong.mall.list.service.SearchService;
import cool.yunlong.mall.mq.constant.MqConst;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听消息
 *
 * @author yunlong
 * @since 2022/6/30 17:10
 */
@Component
public class ListReceiver {

    @Autowired
    private SearchService searchService;

    // 商品上架
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperGoods(Long skuId, Message message, Channel channel) {
        try {
            // 判断 skuId 是否为空
            if (skuId != null) {
                // 执行上架逻辑
                searchService.upperGoods(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 手动消息确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    // 商品下架
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerGoods(Long skuId, Message message, Channel channel) {
        try {
            // 判断 skuId 是否为空
            if (skuId != null) {
                // 执行下架逻辑
                searchService.lowerGoods(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 手动消息确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
