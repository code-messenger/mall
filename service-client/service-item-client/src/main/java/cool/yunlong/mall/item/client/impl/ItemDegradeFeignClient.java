package cool.yunlong.mall.item.client.impl;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/17 11:35
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {

    @Override
    public Result<Map<String, Object>> getItem(Long skuId) {
        return Result.fail();
    }
}

