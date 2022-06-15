package cool.yunlong.mall.item.service.impl;

import cool.yunlong.mall.item.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/14 17:27
 */
@Service
public class ItemServiceImpl implements ItemService {


    /**
     * 根据skuId查询商品详情信息
     *
     * @param skuId skuId
     * @return 商品详情信息
     */
    @Override
    public Map<String, Object> getItemBySkuId(Long skuId) {
        return null;
    }
}

