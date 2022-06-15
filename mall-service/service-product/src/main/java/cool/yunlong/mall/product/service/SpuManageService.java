package cool.yunlong.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.model.product.*;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/13 10:27
 */
public interface SpuManageService {

    /**
     * 分页查询 spu 列表
     *
     * @param pageInfo 分页信息
     * @param spuInfo  spu 数据
     * @return 分页结果
     */
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageInfo, SpuInfo spuInfo);

    /**
     * 查询所有销售属性
     *
     * @return 销售属性列表
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存 spu
     *
     * @param spuInfo spu 信息
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据 spuId 查询 spu 图片列表
     *
     * @param spuId spu 编号
     * @return spu 图片列表
     */
    List<SpuImage> getSpuImageList(Long spuId);

    /**
     * 根据 spuId 查询 spu 销售属性
     *
     * @param spuId spu 编号
     * @return spu 销售属性
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);


    /**
     * 根据spuId查询spu的海报
     *
     * @param spuId spu编号
     * @return spu的海报
     */
    List<SpuPoster> findSpuPosterBySpuId(Long spuId);

    /**
     * 更新 spu
     *
     * @param spuInfo
     */
    void updateSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId查询spu的销售属性信息
     *
     * @param spuId spu编号
     * @return 销售属性信息
     */
    SpuInfo getSpuInfo(Long spuId);
}
