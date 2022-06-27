package cool.yunlong.mall.list.service;

import cool.yunlong.mall.model.list.SearchParam;
import cool.yunlong.mall.model.list.SearchResponseVo;

public interface SearchService {
    /**
     * 商品上架
     *
     * @param skuId sku 编号
     */
    void upperGoods(Long skuId);

    /**
     * 商品下架
     *
     * @param skuId sku 编号
     */
    void lowerGoods(Long skuId);

    /**
     * 记录商品热度排名
     *
     * @param skuId sku编号
     */
    void incrHotScore(Long skuId);

    /**
     * 商品检索
     *
     * @param searchParam 检索条件
     * @return 商品信息
     */
    SearchResponseVo search(SearchParam searchParam);
}
