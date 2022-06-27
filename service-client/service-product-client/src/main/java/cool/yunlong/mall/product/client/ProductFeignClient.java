package cool.yunlong.mall.product.client;

import com.alibaba.fastjson.JSONObject;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.client.impl.ProductDegradeFeignClient;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/15 11:18
 */
@FeignClient(value = "service-product", fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {

    /**
     * 根据 skuId 获取 sku 详情信息
     *
     * @param skuId skuId
     * @return sku 详情信息
     */
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    @Operation(summary = "根据三级分类id查询分类信息")
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);

    /**
     * 根据 skuId 获取 sku 最新价格;    为了保证价格的一致性,这里需要再次查询sku的价格
     *
     * @param skuId skuId
     * @return sku 最新价格
     */
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);

    /**
     * 根据skuId、spuId查询销售属性集合
     *
     * @param skuId skuId
     * @param spuId spuId
     * @return 销售属性集合
     */
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId);

    /**
     * 根据 spuId  查询 sku 销售属性组合
     *
     * @param spuId spuId
     * @return sku 销售属性组合
     */
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    Map<Object, Object> getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    /**
     * 根据 spuId  查询 spu 的海报
     *
     * @param spuId spuId
     * @return spu 的海报
     */
    @GetMapping("/api/product/inner/findSpuPosterBySpuId/{spuId}")
    List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId);

    /**
     * 根据 skuId  获取平台属性集合
     *
     * @param skuId skuId
     * @return sku平台属性集合
     */
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId);

    /**
     * 获取首页分类信息
     *
     * @return 分类信息
     */
    @GetMapping("/api/product/getBaseCategoryList")
    Result<List<JSONObject>> getBaseCategoryList();

    /**
     * 根据品牌 id 获取品牌数据
     *
     * @param tmId 品牌 id
     * @return 品牌数据
     */
    @GetMapping("/api/product/inner/getTrademark/{tmId}")
    BaseTrademark getTrademark(@PathVariable Long tmId);
}
