package cool.yunlong.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.product.BaseSaleAttr;
import cool.yunlong.mall.model.product.SpuImage;
import cool.yunlong.mall.model.product.SpuInfo;
import cool.yunlong.mall.model.product.SpuSaleAttr;
import cool.yunlong.mall.product.service.SpuManageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/13 9:54
 */
@Api(tags = "商品SPU属性接口")
@RestController
@RequestMapping("/admin/product")
public class SpuManageController {

    @Autowired
    private SpuManageService spuManageService;

    @Operation(summary = "分页展示商品Spu信息")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<SpuInfo>> getSpuInfoList(@PathVariable("page") Long page, @PathVariable("limit") Long limit, SpuInfo spuInfo) {
        // 创建分页对象
        Page<SpuInfo> pageInfo = new Page<>(page, limit);
        IPage<SpuInfo> spuInfoPageList = spuManageService.getSpuInfoPage(pageInfo, spuInfo);
        return Result.ok(spuInfoPageList);
    }

    @Operation(summary = "获取销售属性列表")
    @GetMapping("/baseSaleAttrList")
    public Result<List<BaseSaleAttr>> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = spuManageService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    @Operation(summary = "保存spu")
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuManageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    @Operation(summary = "根据spuId获取spu图片列表")
    @GetMapping("/spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable("spuId") Long spuId) {
        List<SpuImage> spuImageList = spuManageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    @Operation(summary = "根据spuId获取spu销售属性")
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuManageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    @Operation(summary = "更新spu")
    @PostMapping("/updateSpuInfo")
    public Result updateSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuManageService.updateSpuInfo(spuInfo);
        return Result.ok();
    }

    @Operation(summary = "根据spuId获取spu信息")
    @GetMapping("/getSpuInfo/{spuId}")
    public Result<SpuInfo> getSpuInfo(@PathVariable("spuId") Long spuId) {
        SpuInfo spuInfo = spuManageService.getSpuInfo(spuId);
        return Result.ok(spuInfo);
    }
}
