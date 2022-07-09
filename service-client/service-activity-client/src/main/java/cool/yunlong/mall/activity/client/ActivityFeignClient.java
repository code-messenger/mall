package cool.yunlong.mall.activity.client;

import cool.yunlong.mall.activity.client.impl.ActivityDegradeFeignClient;
import cool.yunlong.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/7/2 18:59
 */
@FeignClient(value = "service-activity", fallback = ActivityDegradeFeignClient.class)
public interface ActivityFeignClient {

    /**
     * 返回全部列表
     *
     * @return 全部列表
     */
    @GetMapping("/api/activity/seckill/findAll")
    Result findAll();

    /**
     * 获取实体
     *
     * @param skuId 商品ID
     * @return 商品实体
     */
    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    Result getSeckillGoods(@PathVariable("skuId") Long skuId);

    /**
     * 秒杀确认下单
     *
     * @return 结果
     */
    @GetMapping("/api/activity/seckill/auth/trade")
    Result<Map<String, Object>> trade();

}