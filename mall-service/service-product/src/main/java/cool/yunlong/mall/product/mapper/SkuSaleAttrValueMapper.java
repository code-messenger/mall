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
     * 根据spuId获取销售属性值列表
     *
     * @param spuId spu编号
     * @return 销售属性值列表
     */
    List<Map<Object, Object>> selectSaleAttrValuesBySpu(Long spuId);
}
