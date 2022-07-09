package cool.yunlong.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.product.SkuInfo;
import cool.yunlong.mall.product.service.SkuManageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author yunlong
 * @since 2022/6/14 11:16
 */
@Api(tags = "商品SKU属性接口")
@RestController
@RequestMapping("/admin/product")
public class SkuManageController {

    @Autowired
    private SkuManageService skuManageService;

    @Operation(summary = "分页展示sku列表")
    @GetMapping("/list/{page}/{limit}")
    public Result<IPage<SkuInfo>> list(@PathVariable("page") Long page, @PathVariable("limit") Long limit,
                                          SkuInfo skuInfo) {
        Page<SkuInfo> pageInfo = new Page<>(page, limit);
        IPage<SkuInfo> skuInfoIPage = skuManageService.getSkuInfoPage(pageInfo, skuInfo);
        return Result.ok(skuInfoIPage);
    }

    @Operation(summary = "保存sku信息")
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @Operation(summary = "商品上架", description = "根据skuId上架商品")
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        skuManageService.onSale(skuId);
        return Result.ok();
    }

    @Operation(summary = "商品下架", description = "根据skuId下架商品")
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        skuManageService.offSale(skuId);
        return Result.ok();
    }

    @Operation(summary = "更新sku信息")
    @PostMapping("/updateSkuInfo")
    public Result updateSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.updateSkuInfo(skuInfo);
        return Result.ok();
    }

    @Operation(summary = "根据skuId获取sku信息")
    @GetMapping("/getSkuInfo/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId) {
        SkuInfo skuInfo = skuManageService.getAllSkuInfo(skuId);
        return Result.ok(skuInfo);
    }
}
