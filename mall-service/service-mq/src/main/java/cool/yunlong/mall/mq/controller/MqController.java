package cool.yunlong.mall.mq.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.mq.config.DeadLetterMqConfig;
import cool.yunlong.mall.mq.config.DelayedMqConfig;
import cool.yunlong.mall.mq.service.RabbitService;
import io.swagger.annotations.Api;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yunlong
 * @since 2022/6/29 21:51
 */
@Api(tags = "MQ消息接口")
@RestController
@RequestMapping("/mq")
public class MqController {

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendConfirm")
    public Result<String> send() {
        // 发送消息
        rabbitService.sendMsg("exchange.confirm", "routing.confirm.error", "风中追风");
        return Result.ok("消息已发送");
    }

    @GetMapping("sendDeadLettle")
    public Result<String> sendDeadLettle() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.rabbitTemplate.convertAndSend(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, "ok");
        System.out.println(sdf.format(new Date()) + " Delay sent.");
        return Result.ok("消息已发送");
    }

    @GetMapping("/sendDelay")
    public Result<String> sendDelay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        rabbitTemplate.convertAndSend(DelayedMqConfig.DELAYED_EXCHANGE_NAME, DelayedMqConfig.DELAYED_ROUTING_KEY,
//                sdf.format(new Date()), message -> {
//                    message.getMessageProperties().setDelay(10 * 1000);
//                    System.out.println(sdf.format(new Date()) + " Delay sent.");
//                    return message;
//                });
        // 使用工具类
        rabbitService.sendDelayMsg(DelayedMqConfig.DELAYED_EXCHANGE_NAME, DelayedMqConfig.DELAYED_ROUTING_KEY, "且将新火试新茶", 10);
        return Result.ok();
    }
}
