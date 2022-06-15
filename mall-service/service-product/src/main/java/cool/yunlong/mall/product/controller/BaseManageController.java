package cool.yunlong.mall.product.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品基础属性接口")
@RestController
@RequestMapping("/admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;

    /**
     * 获取全部一级分类信息
     *
     * @return 一级分类信息集合
     */
    @Operation(summary = "获取全部一级分类信息")
    @GetMapping("/getCategory1")
    public Result<List<BaseCategory1>> getCategory1() {
        List<BaseCategory1> category1List = manageService.getCategory1();
        return Result.ok(category1List);
    }

    /**
     * 获取指定一级分类下的二级分类信息
     *
     * @param category1Id 一级分类id
     * @return 二级分类信息集合
     */
    @Operation(summary = "获取指定一级分类下的二级分类信息")
    @GetMapping("getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable("category1Id") Long category1Id) {
        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    /**
     * 获取指定二级分类下的三级分类信息
     *
     * @param category2Id 二级分类id
     * @return 三级分类信息集合
     */
    @Operation(summary = "获取指定二级分类下的三级分类信息")
    @GetMapping("getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable("category2Id") Long category2Id) {
        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    /**
     * 获取指定三级分类下的平台属性信息
     *
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     * @return 平台属性信息集合
     */
    @Operation(summary = "获取指定三级分类下的平台属性信息")
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable("category1Id") Long category1Id,
                                                   @PathVariable("category2Id") Long category2Id,
                                                   @PathVariable("category3Id") Long category3Id) {
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(baseAttrInfoList);
    }

    /**
     * 新增/修改平台属性信息
     *
     * @param baseAttrInfo 平台属性信息
     * @return 操作结果
     */
    @Operation(summary = "保存/修改平台属性")
    @PostMapping("saveAttrInfo")
    public Result<String> saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    @Operation(summary = "获取平台属性值信息", description = "根据平台属性 id 查询平台属性值信息")
    @GetMapping("getAttrValueList/{id}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable("id") Long id) {
        // 先通过平台属性 id 查询平台属性信息 再查询平台属性值信息
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(id);
        // 获取平台属性值信息
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        return Result.ok(attrValueList);
    }


}


