package cool.yunlong.mall.task.scheduled;

import cool.yunlong.mall.mq.constant.MqConst;
import cool.yunlong.mall.mq.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author yunlong
 * @since 2022/7/2 18:36
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    /**
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void task1() {
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_1, "");
    }

    /**
     * 每天下午18点执行
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void task18() {
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_18, "");
    }
}
