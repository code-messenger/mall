package cool.yunlong.mall.item.client;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/17 11:31
 */
@FeignClient(value = "service-item", fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {

    /**
     * 根据skuId查询商品详情信息
     *
     * @param skuId skuId
     * @return 商品详情信息
     */
    @GetMapping("/api/item/{skuId}")
    Result<Map<String, Object>> getItem(@PathVariable("skuId") Long skuId);
}
