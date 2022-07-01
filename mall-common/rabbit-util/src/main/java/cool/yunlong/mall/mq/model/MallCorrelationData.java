package cool.yunlong.mall.mq.model;

import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;

/**
 * @author yunlong
 * @since 2022/6/30 15:56
 */
@Data
public class MallCorrelationData extends CorrelationData {

    // 消息主体
    private Object message;

    // 交换机
    private String exchange;

    // 路由键
    private String routingKey;

    // 重试次数
    private int retryCount = 0;

    /**
     * 消息类型 true 代表是 死信消息 false 代表是 正常消息
     */
    private boolean isDelay = false;

    /**
     * 延迟时间  时间单位：s
     */
    private int delayTime = 0;

}
