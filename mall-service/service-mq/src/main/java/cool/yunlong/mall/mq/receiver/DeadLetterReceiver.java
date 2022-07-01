package cool.yunlong.mall.mq.receiver;

import com.rabbitmq.client.Channel;
import cool.yunlong.mall.mq.config.DeadLetterMqConfig;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author yunlong
 * @since 2022/6/30 18:23
 */
@Component
public class DeadLetterReceiver {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void get(String msg, Message message, Channel channel) {

        String msgKey = "delay: " + msg;

        // 保证消息幂等性和一定被消费 借助redis实现
        Boolean result = redisTemplate.opsForValue().setIfAbsent(msgKey, "0", 10, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(result)) {
            String status = redisTemplate.opsForValue().get(msgKey);
            if ("1".equals(status)) {
                // 已经消费过了 手动确认
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            } else {
                // 说明没有消费过，那么就再次消费
                packMsg(msg, message, channel, msgKey);
            }
        }
        packMsg(msg, message, channel, msgKey);
    }

    private void packMsg(String msg, Message message, Channel channel, String msgKey) throws IOException {
        System.out.println("Receive:" + msg);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Receive queue_dead_2: " + sdf.format(new Date()) + " Delay race." + msg);
        // 修改缓存
        redisTemplate.opsForValue().set(msgKey, "1");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
