package cool.yunlong.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.product.BaseTrademark;
import cool.yunlong.mall.product.service.BaseTrademarkService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 品牌管理
 *
 * @author yunlong
 * @since 2022/6/13 10:07
 */
@Api(tags = "品牌管理接口")
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @Operation(summary = "分页展示品牌列表")
    @GetMapping("/{page}/{limit}")
    public Result<IPage<BaseTrademark>> index(@PathVariable("page") Long page, @PathVariable("limit") Long limit) {
        // 创建分页对象
        Page<BaseTrademark> pageParam = new Page<>(page, limit);

        IPage<BaseTrademark> pageModel = baseTrademarkService.getPage(pageParam);
        return Result.ok(pageModel);
    }

    @Operation(summary = "根据id获取品牌信息")
    @GetMapping("/get/{id}")
    public Result<BaseTrademark> get(@PathVariable("id") Long id) {
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    @Operation(summary = "新增品牌信息")
    @PostMapping("/save")
    public Result save(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    @Operation(summary = "更新品牌信息")
    @PutMapping("/update")
    public Result update(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    @Operation(summary = "删除品牌信息")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable("id") Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

}
