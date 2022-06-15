package cool.yunlong.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cool.yunlong.mall.model.product.SpuSaleAttr;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/14 10:39
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    /**
     * 根据 spuId 查询 spu 销售属性
     *
     * @param spuId spu 编号
     * @return spu 销售属性
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(Long spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(Long skuId, Long spuId);
}

