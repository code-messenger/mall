package cool.yunlong.mall.list.service.impl;

import com.alibaba.fastjson.JSON;
import cool.yunlong.mall.list.repository.GoodsRepository;
import cool.yunlong.mall.list.service.SearchService;
import cool.yunlong.mall.model.list.*;
import cool.yunlong.mall.model.product.BaseAttrInfo;
import cool.yunlong.mall.model.product.BaseCategoryView;
import cool.yunlong.mall.model.product.BaseTrademark;
import cool.yunlong.mall.model.product.SkuInfo;
import cool.yunlong.mall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Qualifier("cool.yunlong.mall.product.client.ProductFeignClient")
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 商品上架
     * 从 MySQL 放到 elasticsearch 中
     *
     * @param skuId sku 编号
     */
    @Override
    public void upperGoods(Long skuId) {
        // 创建一个 goods 对象
        Goods goods = new Goods();
        // 设置商品 id
        goods.setId(skuId);

        // 获取 skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 设置商品名称
        goods.setTitle(skuInfo.getSkuName());
        // 设置默认名称
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());

        // 获取 sku 实时价格
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        // 设置商品价格
        goods.setPrice(skuPrice.doubleValue());

        // 获取品牌数据
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        // 设置品牌 id
        goods.setTmId(trademark.getId());
        // 设置品牌名称
        goods.setTmName(trademark.getTmName());
        // 设置 logoUrl
        goods.setTmLogoUrl(trademark.getLogoUrl());

        // 获取分类数据
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        // 设置分类 id
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());

        // 设置分类名称
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());

        // 获取平台属性
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        // 设置商品的平台属性
        List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());

        goods.setAttrs(searchAttrList);

        // 保存方法
        goodsRepository.save(goods);
    }

    /**
     * 商品下架
     *
     * @param skuId sku 编号
     */
    @Override
    public void lowerGoods(Long skuId) {
        // 删除数据
        goodsRepository.deleteById(skuId);
    }

    /**
     * 记录商品热度排名
     *
     * @param skuId sku编号
     */
    @Override
    public void incrHotScore(Long skuId) {
        // 需要借助 redis 来缓冲
        String hotKey = "hotScore";

        // String set hash list zSet
        Double count = redisTemplate.opsForZSet().incrementScore(hotKey, "hot:" + skuId, 1);

        assert count != null;
        // 判断
        if (count % 10 == 0) {
            // 此时更新一下es中的hotScore
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            if (optionalGoods.isPresent()) {
                Goods goods = optionalGoods.get();
                goods.setHotScore(count.longValue());
                goodsRepository.save(goods);
            }
        }
    }

    /**
     * 商品检索
     *
     * @param searchParam 检索条件
     * @return 商品信息
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        // 动态生成 DSL 语句
        SearchRequest searchRequest = searchDsl(searchParam);

        SearchResponse searchResponse = null;
        try {
            // 调用查询方法
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 将结果封装到 SearchResponseVo
        assert searchResponse != null;
        SearchResponseVo searchResponseVo = parseResult(searchResponse);

        searchResponseVo.setPageSize(searchParam.getPageSize());
        searchResponseVo.setPageNo(searchParam.getPageNo());
        // 总页数
        long totalPages = (searchResponseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();

        searchResponseVo.setTotalPages(totalPages);
        // 返回结果
        return searchResponseVo;
    }

    /**
     * 数据结果集转换
     *
     * @param searchResponse 响应结果
     * @return SearchResponseVo 响应结果 Vo
     */
    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        SearchHits hits = searchResponse.getHits();
        // hits total value
        searchResponseVo.setTotal(hits.getTotalHits().value);

        // 创建一个集合用来存储商品
        ArrayList<Goods> goodsList = new ArrayList<>();

        SearchHit[] subHits = hits.getHits();
        // 循环遍历
        for (SearchHit subHit : subHits) {
            // _source 对应的字符串
            String sourceAsString = subHit.getSourceAsString();
            // 将字符串转换为 JSON
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            if (subHit.getHighlightFields().get("title") != null) {
                // 表示有高亮
                Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                // 设置高亮的商品名称
                goods.setTitle(title.toString());
            }
            // 将商品添加到集合中
            goodsList.add(goods);
        }
        searchResponseVo.setGoodsList(goodsList);

        // 获取品牌信息，这些信息都来源于聚合中获取
        // key = tmIdAgg value = Aggregation
        Map<String, Aggregation> stringAggregationMap = searchResponse.getAggregations().asMap();
        // Aggregation 接口
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) stringAggregationMap.get("tmIdAgg");
        // buckets
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            // 声明一个品牌对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();

            // 获取品牌 Id
            String tmId = bucket.getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(tmId));

            // 获取品牌名称
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            // 因为品牌 id 对应的品牌名只有一个 get(0)
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);

            // 获取品牌 logoUrl
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

            // 返回数据
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(trademarkList);

        // 获取平台属性数据 数据类型 nested
        ParsedNested attrAgg = (ParsedNested) stringAggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrList = attrIdAgg.getBuckets().stream().map(bucket -> {
            // 创建平台属性对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

            // 获取平台属性 id
            String attrId = bucket.getKeyAsString();
            searchResponseAttrVo.setAttrId(Long.parseLong(attrId));

            // 获取平台属性名称
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            // 因为平台属性 id 对应的平台属性名只有一个 get(0)
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            // 获取平台属性值名称
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            // 通过 map 映射到平台属性值名称上
            List<String> valueNameList = attrValueAgg.getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(valueNameList);

            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(attrList);

        // 返回数据
        return searchResponseVo;
    }

    /**
     * 动态生成 DSL 语句
     *
     * @param searchParam 检索条件
     * @return DSL 语句
     */
    private SearchRequest searchDsl(SearchParam searchParam) {
        // 构建一个查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // { query bool }
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 判断是否根据关键词检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            // { bool must match }
            boolQuery.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));

            // 设置高亮 { highlight }
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style=color:red>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        // 根据分类 id 进行检索
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            // { query bool filter }
            boolQuery.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            // { query bool filter }
            boolQuery.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            // { query bool filter }
            boolQuery.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }

        // 按照品牌进行过滤
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            // trademark=3:华为
            String[] split = searchParam.getTrademark().split(":");
            if (split.length == 2) {
                //  {query bool filter term }
                boolQuery.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //  根据平台属性值 id 进行过滤
        //  props=23:8G:运行内存&props=24:256G:机身内存
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            //  循环遍历
            for (String prop : props) {
                // prop  props=23:8G:运行内存
                String[] split = prop.split(":");
                if (split.length == 3) {
                    //  构建 dsl 语句. 外层
                    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                    BoolQueryBuilder subBoolBuilder = QueryBuilders.boolQuery();

                    subBoolBuilder.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolBuilder.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                    //  subBoolBuilder ---> boolQueryBuilder
                    boolQueryBuilder.must(QueryBuilders.nestedQuery("attrs", subBoolBuilder, ScoreMode.None));
                    boolQuery.filter(boolQueryBuilder);
                }
            }
        }
        //  { query }
        searchSourceBuilder.query(boolQuery);

        // 排序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            // 分割字符串
            String[] split = order.split(":");
            if (split.length == 2) {
                // 声明一个变量 记录按照什么排序
                String field = "";
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                // 第一个参数：排序的字段 第二个参数：ASC / DESC
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            } else {
                // 默认排序
                searchSourceBuilder.sort("hotScore", SortOrder.DESC);
            }
        }

        // 分页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        // 聚合： 品牌   field: term
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        // 聚合： 平台属性     field: nested
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")))
        );

        // 声明一个对象
        SearchRequest searchRequest = new SearchRequest("goods");

        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price", "createTime"}, null);

        // 把整个query,sort,from 所有的 dsl都放人 source 中
        searchRequest.source(searchSourceBuilder);
        System.out.println("dsl: " + searchSourceBuilder);
        // 返回对象
        return searchRequest;
    }
}
