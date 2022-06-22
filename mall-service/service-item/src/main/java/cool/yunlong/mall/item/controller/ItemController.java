package cool.yunlong.mall.item.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.item.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 前台商品详情接口
 * <p>
 * 负责商品详情页面的展示,提供给 web-all 调用
 * <p>
 * user --> web-all --> service-item --> service-product-client --> service-product
 *
 * @author yunlong
 * @since 2022/6/14 17:53
 */
@Api("商品详情信息")
@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Operation(summary = "获取 sku 详情信息")
    @GetMapping("/{skuId}")
    public Result<Map<String, Object>> getItem(@PathVariable Long skuId) {
        Map<String, Object> result = itemService.getItemBySkuId(skuId);
        return Result.ok(result);
    }

}
