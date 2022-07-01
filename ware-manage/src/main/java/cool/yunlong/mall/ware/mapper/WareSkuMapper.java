package cool.yunlong.mall.ware.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cool.yunlong.mall.ware.bean.WareSku;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WareSkuMapper extends BaseMapper<WareSku> {

    public Integer selectStockBySkuId(String skuId);

    public int incrStockLocked(WareSku wareSku);

    public int selectStockBySkuIdForUpdate(WareSku wareSku);

    public int deliveryStock(WareSku wareSku);

    public List<WareSku> selectWareSkuAll();
}
