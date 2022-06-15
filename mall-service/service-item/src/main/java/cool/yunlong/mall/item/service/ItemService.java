package cool.yunlong.mall.item.service;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/14 17:26
 */
public interface ItemService {
    /**
     * 根据skuId查询商品详情信息
     *
     * @param skuId skuId
     * @return 商品详情信息
     */
    Map<String, Object> getItemBySkuId(Long skuId);


}
