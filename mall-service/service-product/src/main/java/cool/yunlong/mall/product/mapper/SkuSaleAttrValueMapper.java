package cool.yunlong.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cool.yunlong.mall.model.product.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/14 11:15
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * 商品切换
     *
     * @param spuId spu编号
     * @return 商品切换
     */
    List<Map<Object, Object>> selectSaleAttrValuesBySpu(Long spuId);
}
