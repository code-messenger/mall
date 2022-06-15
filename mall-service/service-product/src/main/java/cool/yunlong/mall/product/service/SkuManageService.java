package cool.yunlong.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.model.product.BaseAttrInfo;
import cool.yunlong.mall.model.product.BaseCategoryView;
import cool.yunlong.mall.model.product.SkuInfo;
import cool.yunlong.mall.model.product.SpuSaleAttr;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/14 11:17
 */
public interface SkuManageService {

    /**
     * 保存sku信息
     *
     * @param skuInfo sku信息
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 分页查询sku列表
     *
     * @param pageInfo 分页信息
     * @param skuInfo  sku信息
     * @return 分页结果
     */
    IPage<SkuInfo> getSkuInfoPage(Page<SkuInfo> pageInfo, SkuInfo skuInfo);

    /**
     * 商品上架
     *
     * @param skuId sku编号
     */
    void onSale(Long skuId);

    /**
     * 商品下架
     *
     * @param skuId sku编号
     */
    void offSale(Long skuId);

    /**
     * 根据skuId查询商品详情信息
     *
     * @param skuId sku编号
     * @return 商品详情信息
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据三级分类id查询分类列表
     *
     * @param category3Id 三级分类id
     * @return 分类列表
     */
    BaseCategoryView getCategoryView(Long category3Id);

    /**
     * 根据skuId查询sku的价格
     *
     * @param skuId sku编号
     * @return sku的价格
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 根据spuId、skuId 查询销售属性列表
     *
     * @param skuId sku编号
     * @param spuId spu编号
     * @return 销售属性列表
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 根据spuId查询sku的销售属性值列表
     *
     * @param spuId spu编号
     * @return 销售属性值列表
     */
    Map<Object, Object> getSkuValueIdsMap(Long spuId);

    /**
     * 根据skuId查询平台属性列表
     *
     * @param skuId sku编号
     * @return 平台属性列表
     */
    List<BaseAttrInfo> getAttrList(Long skuId);

    /**
     * 更新sku信息
     *
     * @param skuInfo sku信息
     */
    void updateSkuInfo(SkuInfo skuInfo);
}
