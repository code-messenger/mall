package cool.yunlong.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.model.order.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author yunlong
 * @since 2022/6/28 22:53
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    IPage<OrderInfo> selectOrderPage(@Param("pageParam") Page<OrderInfo> pageParam, @Param("userId") String userId);
}
