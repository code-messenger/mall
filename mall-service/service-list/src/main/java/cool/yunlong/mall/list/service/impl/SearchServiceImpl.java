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
     * ????????????
     * ??? MySQL ?????? elasticsearch ???
     *
     * @param skuId sku ??????
     */
    @Override
    public void upperGoods(Long skuId) {
        // ???????????? goods ??????
        Goods goods = new Goods();
        // ???????????? id
        goods.setId(skuId);

        // ?????? skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // ??????????????????
        goods.setTitle(skuInfo.getSkuName());
        // ??????????????????
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());

        // ?????? sku ????????????
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        // ??????????????????
        goods.setPrice(skuPrice.doubleValue());

        // ??????????????????
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        // ???????????? id
        goods.setTmId(trademark.getId());
        // ??????????????????
        goods.setTmName(trademark.getTmName());
        // ?????? logoUrl
        goods.setTmLogoUrl(trademark.getLogoUrl());

        // ??????????????????
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        // ???????????? id
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());

        // ??????????????????
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());

        // ??????????????????
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        // ???????????????????????????
        List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());

        goods.setAttrs(searchAttrList);

        // ????????????
        goodsRepository.save(goods);
    }

    /**
     * ????????????
     *
     * @param skuId sku ??????
     */
    @Override
    public void lowerGoods(Long skuId) {
        // ????????????
        goodsRepository.deleteById(skuId);
    }

    /**
     * ????????????????????????
     *
     * @param skuId sku??????
     */
    @Override
    public void incrHotScore(Long skuId) {
        // ???????????? redis ?????????
        String hotKey = "hotScore";

        // String set hash list zSet
        Double count = redisTemplate.opsForZSet().incrementScore(hotKey, "hot:" + skuId, 1);

        assert count != null;
        // ??????
        if (count % 10 == 0) {
            // ??????????????????es??????hotScore
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            if (optionalGoods.isPresent()) {
                Goods goods = optionalGoods.get();
                goods.setHotScore(count.longValue());
                goodsRepository.save(goods);
            }
        }
    }

    /**
     * ????????????
     *
     * @param searchParam ????????????
     * @return ????????????
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        // ???????????? DSL ??????
        SearchRequest searchRequest = searchDsl(searchParam);

        SearchResponse searchResponse = null;
        try {
            // ??????????????????
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ?????????????????? SearchResponseVo
        assert searchResponse != null;
        SearchResponseVo searchResponseVo = parseResult(searchResponse);

        searchResponseVo.setPageSize(searchParam.getPageSize());
        searchResponseVo.setPageNo(searchParam.getPageNo());
        // ?????????
        long totalPages = (searchResponseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();

        searchResponseVo.setTotalPages(totalPages);
        // ????????????
        return searchResponseVo;
    }

    /**
     * ?????????????????????
     *
     * @param searchResponse ????????????
     * @return SearchResponseVo ???????????? Vo
     */
    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        SearchHits hits = searchResponse.getHits();
        // hits total value
        searchResponseVo.setTotal(hits.getTotalHits().value);

        // ????????????????????????????????????
        ArrayList<Goods> goodsList = new ArrayList<>();

        SearchHit[] subHits = hits.getHits();
        // ????????????
        for (SearchHit subHit : subHits) {
            // _source ??????????????????
            String sourceAsString = subHit.getSourceAsString();
            // ????????????????????? JSON
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            if (subHit.getHighlightFields().get("title") != null) {
                // ???????????????
                Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                // ???????????????????????????
                goods.setTitle(title.toString());
            }
            // ???????????????????????????
            goodsList.add(goods);
        }
        searchResponseVo.setGoodsList(goodsList);

        // ????????????????????????????????????????????????????????????
        // key = tmIdAgg value = Aggregation
        Map<String, Aggregation> stringAggregationMap = searchResponse.getAggregations().asMap();
        // Aggregation ??????
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) stringAggregationMap.get("tmIdAgg");
        // buckets
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            // ????????????????????????
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();

            // ???????????? Id
            String tmId = bucket.getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(tmId));

            // ??????????????????
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            // ???????????? id ?????????????????????????????? get(0)
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);

            // ???????????? logoUrl
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

            // ????????????
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(trademarkList);

        // ???????????????????????? ???????????? nested
        ParsedNested attrAgg = (ParsedNested) stringAggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrList = attrIdAgg.getBuckets().stream().map(bucket -> {
            // ????????????????????????
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

            // ?????????????????? id
            String attrId = bucket.getKeyAsString();
            searchResponseAttrVo.setAttrId(Long.parseLong(attrId));

            // ????????????????????????
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            // ?????????????????? id ???????????????????????????????????? get(0)
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            // ???????????????????????????
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            // ?????? map ?????????????????????????????????
            List<String> valueNameList = attrValueAgg.getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(valueNameList);

            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(attrList);

        // ????????????
        return searchResponseVo;
    }

    /**
     * ???????????? DSL ??????
     *
     * @param searchParam ????????????
     * @return DSL ??????
     */
    private SearchRequest searchDsl(SearchParam searchParam) {
        // ?????????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // { query bool }
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // ?????????????????????????????????
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            // { bool must match }
            boolQuery.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));

            // ???????????? { highlight }
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style=color:red>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        // ???????????? id ????????????
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

        // ????????????????????????
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            // trademark=3:??????
            String[] split = searchParam.getTrademark().split(":");
            if (split.length == 2) {
                //  {query bool filter term }
                boolQuery.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //  ????????????????????? id ????????????
        //  props=23:8G:????????????&props=24:256G:????????????
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            //  ????????????
            for (String prop : props) {
                // prop  props=23:8G:????????????
                String[] split = prop.split(":");
                if (split.length == 3) {
                    //  ?????? dsl ??????. ??????
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

        // ??????
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            // ???????????????
            String[] split = order.split(":");
            if (split.length == 2) {
                // ?????????????????? ????????????????????????
                String field = "";
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                // ????????????????????????????????? ??????????????????ASC / DESC
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            } else {
                // ????????????
                searchSourceBuilder.sort("hotScore", SortOrder.DESC);
            }
        }

        // ??????
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        // ????????? ??????   field: term
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        // ????????? ????????????     field: nested
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")))
        );

        // ??????????????????
        SearchRequest searchRequest = new SearchRequest("goods");

        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price", "createTime"}, null);

        // ?????????query,sort,from ????????? dsl????????? source ???
        searchRequest.source(searchSourceBuilder);
        System.out.println("dsl: " + searchSourceBuilder);
        // ????????????
        return searchRequest;
    }
}
