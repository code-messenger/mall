package cool.yunlong.mall.list.client.impl;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.list.client.ListFeignClient;
import cool.yunlong.mall.model.list.SearchParam;
import cool.yunlong.mall.model.list.SearchResponseVo;
import org.springframework.stereotype.Component;

/**
 * @author yunlong
 * @since 2022/6/24 22:16
 */
@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public Result<Void> incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public Result<Void> lowerGoods(Long skuId) {
        return null;
    }

    @Override
    public Result<Void> upperGoods(Long skuId) {
        return null;
    }

    @Override
    public Result<SearchResponseVo> List(SearchParam searchParam) {
        return null;
    }
}
