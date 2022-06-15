package cool.yunlong.mall.product.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.product.BaseTrademark;
import cool.yunlong.mall.model.product.CategoryTrademarkVo;
import cool.yunlong.mall.product.service.BaseCategoryTrademarkService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/13 12:06
 */
@Api(tags = "分类品牌管理接口")
@RestController
@RequestMapping("admin/product/baseCategoryTrademark")
public class BaseCategoryTrademarkController {

    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;

    @Operation(summary = "保存分类品牌关联")
    @PostMapping("/save")
    public Result save(@RequestBody CategoryTrademarkVo categoryTrademarkVo) {
        baseCategoryTrademarkService.saveBaseCategoryTrademark(categoryTrademarkVo);
        return Result.ok();
    }

    @Operation(summary = "删除分类品牌关联")
    @DeleteMapping("/remove/{category3Id}/{trademarkId}")
    public Result remove(@PathVariable("category3Id") Long category3Id, @PathVariable("trademarkId") Long trademarkId) {
        baseCategoryTrademarkService.removeBaseCategoryTrademarkById(category3Id, trademarkId);
        return Result.ok();
    }

    @Operation(summary = "根据三级分类id获取品牌列表")
    @GetMapping("/findTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findTrademarkList(@PathVariable("category3Id") Long category3Id) {
        List<BaseTrademark> list = baseCategoryTrademarkService.findTrademarkList(category3Id);
        return Result.ok(list);
    }

    @Operation(summary = "根据三级分类id获取可选品牌列表")
    @GetMapping("/findCurrentTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findCurrentTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> list = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(list);
    }
}
