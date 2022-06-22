package cool.yunlong.mall.product.client.impl;

import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/17 10:57
 */
@Component
public class ProductDegradeFeignClient implements ProductFeignClient {
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    /**
     * 根据 skuId 获取 sku 最新价格;    为了保证价格的一致性,这里需要再次查询sku的价格
     *
     * @param skuId skuId
     * @return sku 最新价格
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return null;
    }

    /**
     * 根据skuId、spuId查询销售属性集合
     *
     * @param skuId skuId
     * @param spuId spuId
     * @return 销售属性集合
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return null;
    }

    /**
     * 根据 spuId  查询 sku 销售属性组合
     *
     * @param spuId spuId
     * @return sku 销售属性组合
     */
    @Override
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        return null;
    }

    /**
     * 根据 spuId  查询 spu 的海报
     *
     * @param spuId spuId
     * @return spu 的海报
     */
    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        return null;
    }

    /**
     * 根据 skuId  获取平台属性集合
     *
     * @param skuId skuId
     * @return sku平台属性集合
     */
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return null;
    }
}
