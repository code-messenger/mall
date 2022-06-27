package cool.yunlong.mall.list.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.list.service.SearchService;
import cool.yunlong.mall.model.list.Goods;
import cool.yunlong.mall.model.list.SearchParam;
import cool.yunlong.mall.model.list.SearchResponseVo;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

@Api(tags = "商品检索接口")
@RestController
@RequestMapping("/api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private SearchService searchService;

    @Operation(summary = "创建商品索引")
    @GetMapping("inner/createIndex")
    public Result<String> createIndexAction() {
        // 创建索引
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);

        // 响应结果
        return Result.ok("goods 索引创建成功");
    }

    @Operation(summary = "商品上架", description = "根据 skuId 进行商品上架")
    @GetMapping("inner/upperGoods/{skuId}")
    public Result<Void> upperGoods(@PathVariable Long skuId) {
        // 上架商品
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    @Operation(summary = "商品下架", description = "根据 skuId 进行商品下架")
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result<Void> lowerGoods(@PathVariable Long skuId) {
        // 下架商品
        searchService.lowerGoods(skuId);
        return Result.ok();
    }

    @Operation(summary = "商品热度排名", description = "根据 skuId 进行商品热度统计")
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result<Void> incrHotScore(@PathVariable Long skuId) {
        // 调用服务层方法
        searchService.incrHotScore(skuId);
        return Result.ok();
    }

    @Operation(summary = "商品检索", description = "根据条件进行商品检索")
    @PostMapping
    public Result<SearchResponseVo> List(@RequestBody SearchParam searchParam) {
        // 调用服务层方法
        SearchResponseVo searchResponseVo = searchService.search(searchParam);
        return Result.ok(searchResponseVo);
    }
}
