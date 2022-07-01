package cool.yunlong.mall.ware.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cool.yunlong.mall.ware.bean.WareInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WareInfoMapper extends BaseMapper<WareInfo> {

    List<WareInfo> selectWareInfoBySkuId(String skuId);

}
