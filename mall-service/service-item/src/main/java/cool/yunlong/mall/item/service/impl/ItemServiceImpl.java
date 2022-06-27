package cool.yunlong.mall.item.service.impl;

import com.alibaba.fastjson.JSON;
import cool.yunlong.mall.item.service.ItemService;
import cool.yunlong.mall.list.client.ListFeignClient;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author yunlong
 * @since 2022/6/14 17:27
 */
@Service
public class ItemServiceImpl implements ItemService {

    // 远程调用 service-product-client 接口
    @Qualifier("cool.yunlong.mall.product.client.ProductFeignClient")
    @Autowired
    private ProductFeignClient productFeignClient;

//    @Autowired
//    private RedissonClient redissonClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Qualifier("cool.yunlong.mall.list.client.ListFeignClient")
    @Autowired
    private ListFeignClient listFeignClient;


    /**
     * 根据skuId查询商品详情信息
     *
     * @param skuId skuId
     * @return 商品详情信息
     */
    @Override
    public Map<String, Object> getItemBySkuId(Long skuId) {
        // 定义返回结果
        Map<String, Object> result = new HashMap<>();

        // 防止缓存穿透，查询布隆过滤器中是否存在
//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);

//        if (!bloomFilter.contains(skuId)) {
//            // 不存在直接返回 null
//            return null;
//        }
        // 创建一个对象
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {

            // 调用 service-product-client 接口
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

            if (skuInfo == null) {
                return null;

            } else {
                // 保存数据
                result.put("skuInfo", skuInfo);

                // 返回结果
                return skuInfo;
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            // 获取分类数据
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            // 保存数据
            result.put("categoryView", categoryView);
        }, threadPoolExecutor);

        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            // 获取sku的价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            // 保存数据
            result.put("price", skuPrice);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            // 获取销售属性 + 销售属性值
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            // 保存数据
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, threadPoolExecutor);

        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            // 获取销售属性组合
            Map<Object, Object> skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String valueJson = JSON.toJSONString(skuValueIdsMap);
            // 保存数据
            result.put("valuesSkuJson", valueJson);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuPosterListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            // 获取spu海报数据
            List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            // 保存数据
            result.put("spuPosterList", spuPosterList);
        }, threadPoolExecutor);

        CompletableFuture<Void> skuAttrListCompletableFuture = CompletableFuture.runAsync(() -> {
            // 获取sku规格参数
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            List<Map<String, String>> skuAttrList = attrList.stream().map((baseAttrInfo) -> {
                Map<String, String> attrMap = new HashMap<>();
                attrMap.put("attrName", baseAttrInfo.getAttrName());
                attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return attrMap;
            }).collect(Collectors.toList());
            // 保存数据
            result.put("skuAttrList", skuAttrList);
        }, threadPoolExecutor);

        // 调用热度排名方法
        CompletableFuture<Void> incrHostCompletableFuture = CompletableFuture
                .runAsync(() -> listFeignClient.incrHotScore(skuId), threadPoolExecutor);

        // 多任务组合
        CompletableFuture.allOf(skuInfoCompletableFuture, categoryViewCompletableFuture,
                        spuSaleAttrListCompletableFuture, valuesSkuJsonCompletableFuture,
                        priceCompletableFuture, spuPosterListCompletableFuture,
                        skuAttrListCompletableFuture, incrHostCompletableFuture)
                .join();

        // 返回数据
        return result;
    }
}

