package cool.yunlong.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cool.yunlong.mall.model.product.BaseCategoryTrademark;
import cool.yunlong.mall.model.product.BaseTrademark;
import cool.yunlong.mall.model.product.CategoryTrademarkVo;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/13 11:21
 */
public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {

    /**
     * 保存分类品牌关联
     *
     * @param categoryTrademarkVo 分类品牌关联
     */
    void saveBaseCategoryTrademark(CategoryTrademarkVo categoryTrademarkVo);

    /**
     * 删除分类品牌关联
     *
     * @param category3Id 三级分类id
     * @param trademarkId 品牌id
     */
    void removeBaseCategoryTrademarkById(Long category3Id, Long trademarkId);

    /**
     * 根据三级分类id查询品牌列表
     *
     * @param category3Id 三级分类id
     * @return 品牌列表
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);

    /**
     * 根据三级分类id查询可选品牌列表
     *
     * @param category3Id 三级分类id
     * @return 可选品牌列表
     */
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);
}
