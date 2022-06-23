package cool.yunlong.mall.item.service.impl;

import com.alibaba.fastjson.JSON;
import cool.yunlong.mall.common.constant.RedisConst;
import cool.yunlong.mall.item.service.ItemService;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.client.ProductFeignClient;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private RedissonClient redissonClient;


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
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);

//        if (!bloomFilter.contains(skuId)) {
//            // 不存在直接返回 null
//            return null;
//        }

        // 调用 service-product-client 接口
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo != null) {
            // 获取分类数据
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

            // 获取销售属性 + 销售属性值
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());

            // 获取销售属性组合
            Map<Object, Object> skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String valueJson = JSON.toJSONString(skuValueIdsMap);

            // 获取sku的价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);

            // 获取spu海报数据
            List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());

            // 获取sku规格参数
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            List<Map<String, String>> skuAttrList = attrList.stream().map((baseAttrInfo) -> {
                Map<String, String> attrMap = new HashMap<>();
                attrMap.put("attrName", baseAttrInfo.getAttrName());
                attrMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return attrMap;
            }).collect(Collectors.toList());

            // 封装返回结果
            result.put("categoryView", categoryView);
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
            result.put("valuesSkuJson", valueJson);
            result.put("price", skuPrice);
            result.put("spuPosterList", spuPosterList);
            result.put("skuAttrList", skuAttrList);
            result.put("skuInfo", skuInfo);
            return result;
        }
        return null;
    }
}

