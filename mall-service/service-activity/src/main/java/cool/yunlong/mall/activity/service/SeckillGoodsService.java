package cool.yunlong.mall.activity.service;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.activity.SeckillGoods;
import cool.yunlong.mall.model.order.OrderInfo;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/7/2 18:55
 */
public interface SeckillGoodsService {

    /**
     * 返回全部列表
     *
     * @return 全部列表
     */
    List<SeckillGoods> findAll();

    /**
     * 根据ID获取实体
     *
     * @param id 实体ID
     * @return 实体
     */
    SeckillGoods getSeckillGoods(Long id);

    /**
     * 根据用户和商品ID实现秒杀下单
     *
     * @param skuId  商品ID
     * @param userId 用户ID
     */
    void seckillOrder(Long skuId, String userId);

    /**
     * 检查订单状态
     *
     * @param skuId  商品ID
     * @param userId 用户ID
     */
    Result checkOrder(String userId, Long skuId);

    Result getResult(String userId);

    Result getOrderId(OrderInfo orderInfo, String userId);
}
