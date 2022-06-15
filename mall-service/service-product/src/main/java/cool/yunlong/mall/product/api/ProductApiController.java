package cool.yunlong.mall.product.api;

import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.service.SkuManageService;
import cool.yunlong.mall.product.service.SpuManageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 前台商品详情接口
 *
 * @author yunlong
 * @since 2022/6/14 19:50
 */
@Api("前台商品详情接口")
@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    @Autowired
    private SkuManageService skuManageService;

    @Autowired
    private SpuManageService spuManageService;

    @Operation(summary = "根据skuId获取sku信息")
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getAttrValueList(@PathVariable("skuId") Long skuId) {
        return skuManageService.getSkuInfo(skuId);
    }

    @Operation(summary = "通过三级分类id查询分类信息")
    @GetMapping("/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {
        return skuManageService.getCategoryView(category3Id);
    }

    @Operation(summary = "根据skuId获取sku价格")
    @GetMapping("/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId) {
        return skuManageService.getSkuPrice(skuId);
    }

    @Operation(summary = "根据skuId、spuId获取销售属性列表")
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId) {
        return skuManageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @Operation(summary = "根据spuId获取销售属性值列表")
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Map<Object, Object> getSkuValueIdsMap(@PathVariable("spuId") Long spuId) {
        return skuManageService.getSkuValueIdsMap(spuId);
    }

    @Operation(summary = "获取商品海报")
    @GetMapping("/inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId) {
        return spuManageService.findSpuPosterBySpuId(spuId);
    }

    @Operation(summary = "获取sku平台属性列表")
    @GetMapping("/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId) {
        return skuManageService.getAttrList(skuId);
    }
}

