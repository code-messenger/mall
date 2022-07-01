package cool.yunlong.mall.mq.common;

import com.alibaba.fastjson.JSON;
import cool.yunlong.mall.mq.model.MallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author yunlong
 * @since 2022/6/30 11:15
 */
@Slf4j
@Component
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 初始化方法
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 当消息发送到交换机时回调方法
     *
     * @param correlationData 发送的消息
     * @param ack             是否成功
     * @param cause           原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            //  消息发送成功
            log.info("消息发送到交换机成功，id：{}", correlationData.getId());
        } else {
            //  消息发送失败
            log.error("消息发送到交换机失败，正在进行消息重试！ id：{}", correlationData.getId());
            // 交换机没有收到消息，进行消息重试
            retryMsg(correlationData);
        }
    }


    /**
     * 当消息没有发送到队列时触发的回调方法   使用死信队列插件也会触发该方法
     *
     * @param message    发送的消息
     * @param code       应答码
     * @param codeText   描述
     * @param exchange   交换机
     * @param routingKey 路由键
     */
    @Override
    public void returnedMessage(Message message, int code, String codeText, String exchange, String routingKey) {
        log.info("消息主体：{}", new String(message.getBody()));
        log.info("应答码：{}", code);
        log.error("描述：{}", codeText);
        log.info("交换机：{}", exchange);
        log.info("路由键：{}", routingKey);

        // 获取 correlationData 对象的 Id
        String correlationDataId = (String) message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");

        // 通过 correlationDataId 获取 redis 中缓存的 MallCorrelationData 对象
        String strJson = redisTemplate.opsForValue().get(correlationDataId);
        MallCorrelationData mallCorrelationData = JSON.parseObject(strJson, MallCorrelationData.class);

        // 对列没有收到消息，进行消息重试
        log.info("消息发送到队列失败，正在进行消息重试! mallCorrelationData: {}", mallCorrelationData);
        retryMsg(mallCorrelationData);
    }

    /**
     * 消息重试
     *
     * @param correlationData 消息对象
     */
    private void retryMsg(CorrelationData correlationData) {
        // 转换为 MallCorrelationData 对象
        MallCorrelationData mallCorrelationData = (MallCorrelationData) correlationData;

        // 获取重试次数   初始值为 0
        int retryCount = mallCorrelationData.getRetryCount();
        // 判断
        if (retryCount >= 3) {
            // 超过三次，不再重试，消息发送失败
            log.error("重试超过限制，消息发送失败，mallCorrelationData：{}", JSON.toJSONString(mallCorrelationData));
        } else {
            // 重试次数加 1
            retryCount += 1;
            // 更新重试次数
            mallCorrelationData.setRetryCount(retryCount);
            log.info("消息重试了：{}", retryCount + "次");

            // 更新 redis 中的数据
            redisTemplate.opsForValue().set(Objects.requireNonNull(mallCorrelationData.getId()), JSON.toJSONString(mallCorrelationData), 10, TimeUnit.MINUTES);

            // 判断是否时是延迟消息
            if (mallCorrelationData.isDelay()) {
                // 延迟消息
                rabbitTemplate.convertAndSend(mallCorrelationData.getExchange(), mallCorrelationData.getRoutingKey(), mallCorrelationData.getMessage(), message -> {
                    // 设置消息的延迟时间
                    message.getMessageProperties().setDelay(mallCorrelationData.getDelayTime() * 1000);
                    return message;
                }, mallCorrelationData);
            } else {
                // 重发消息
                rabbitTemplate.convertAndSend(mallCorrelationData.getExchange(), mallCorrelationData.getRoutingKey(), mallCorrelationData.getMessage(), mallCorrelationData);
            }
        }
    }
}
