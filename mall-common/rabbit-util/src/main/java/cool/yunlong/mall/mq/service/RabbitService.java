package cool.yunlong.mall.mq.service;

import com.alibaba.fastjson.JSON;
import cool.yunlong.mall.mq.model.MallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yunlong
 * @since 2022/6/29 21:42
 */
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void sendMsg(String exchange, String routingKey, Object msg) {
        // 创建一个mallCorrelationData对象
        MallCorrelationData mallCorrelationData = new MallCorrelationData();
        // 声明一个UUID作为唯一的消息ID
        String correlationDataId = UUID.randomUUID().toString().replaceAll("-", "");
        // 设置消息ID
        mallCorrelationData.setId(correlationDataId);
        // 设置交换机
        mallCorrelationData.setExchange(exchange);
        // 设置路由键
        mallCorrelationData.setRoutingKey(routingKey);
        // 设置消息体
        mallCorrelationData.setMessage(msg);

        // 发送消息时，将 mallCorrelationData 对象放入缓存中
        redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(mallCorrelationData), 10, TimeUnit.MINUTES);

        // 发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, mallCorrelationData);

    }

    /***
     * 发送延迟消息
     * @param exchange  交换机
     * @param routingKey    路由键
     * @param msg    消息体
     * @param delayTime 延迟时间
     */
    public void sendDelayMsg(String exchange, String routingKey, Object msg, int delayTime) {
        // 创建一个mallCorrelationData对象
        MallCorrelationData mallCorrelationData = new MallCorrelationData();
        // 声明一个UUID作为唯一的消息ID
        String correlationDataId = UUID.randomUUID().toString().replaceAll("-", "");
        // 设置消息ID
        mallCorrelationData.setId(correlationDataId);
        // 设置交换机
        mallCorrelationData.setExchange(exchange);
        // 设置路由键
        mallCorrelationData.setRoutingKey(routingKey);
        // 设置消息体
        mallCorrelationData.setMessage(msg);
        // 设置消息类型
        mallCorrelationData.setDelay(true);
        // 设置延迟时间
        mallCorrelationData.setDelayTime(delayTime);

        // 将 mallCorrelationData 对象放入缓存中
        redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(mallCorrelationData), 10, TimeUnit.MINUTES);

        // 发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, message -> {
            // 设置延迟时间
            message.getMessageProperties().setDelay(delayTime * 1000);
            return message;
        }, mallCorrelationData);
    }
}
