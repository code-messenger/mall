package cool.yunlong.mall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


/**
 * @author yunlong
 * @since 2022/6/29 21:58
 */
@Component
public class ConfirmReceiver {

    // 监听消息
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm", durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = "exchange.confirm", autoDelete = "true"),
            key = {"routing.confirm"}
    ))
    public void receive(String msg, Message message, Channel channel) {
        System.out.println("发送的消息内容: " + msg);
        byte[] body = message.getBody();
        System.out.println("接收的消息为: " + new String(body));

        // 开启消息确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
