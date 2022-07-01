package cool.yunlong.mall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * @author yunlong
 * @since 2022/6/30 18:33
 */
@Configuration
public class DelayedMqConfig {

    public static final String DELAYED_QUEUE_NAME = "delayed_queue";
    public static final String DELAYED_EXCHANGE_NAME = "delayed_exchange";
    public static final String DELAYED_ROUTING_KEY = "delayed_routing_key";

    @Bean
    public Queue delayedQueue() {
        return new Queue(DELAYED_QUEUE_NAME, true);
    }

    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayedQueue()).to(delayExchange()).with(DELAYED_ROUTING_KEY).noargs();
    }

}
