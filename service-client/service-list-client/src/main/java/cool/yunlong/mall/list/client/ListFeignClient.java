package cool.yunlong.mall.list.client;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.list.client.impl.ListDegradeFeignClient;
import cool.yunlong.mall.model.list.SearchParam;
import cool.yunlong.mall.model.list.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author yunlong
 * @since 2022/6/24 22:15
 */
@FeignClient(value = "service-list", fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    Result<Void> incrHotScore(@PathVariable Long skuId);

    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    Result<Void> lowerGoods(@PathVariable Long skuId);

    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    Result<Void> upperGoods(@PathVariable Long skuId);

    @PostMapping("/api/list")
    Result<SearchResponseVo> List(@RequestBody SearchParam searchParam);
}
