package cool.yunlong.mall.activity.client.impl;

import cool.yunlong.mall.activity.client.ActivityFeignClient;
import cool.yunlong.mall.common.result.Result;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/7/2 18:59
 */
@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {


    @Override
    public Result findAll() {
        return Result.fail();
    }

    @Override
    public Result getSeckillGoods(Long skuId) {
        return Result.fail();
    }

    /**
     * 秒杀确认下单
     *
     * @return 结果
     */
    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }
}