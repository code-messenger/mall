package cool.yunlong.mall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cool.yunlong.mall.model.enums.ProcessStatus;
import cool.yunlong.mall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/28 22:37
 */
public interface OrderService extends IService<OrderInfo> {

    /**
     * 保存订单
     *
     * @param orderInfo 订单信息
     * @return 订单 id
     */
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * 生产流水号
     *
     * @param userId 用户 id
     * @return 流水号
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     *
     * @param userId      获取缓存中的流水号
     * @param tradeCodeNo 页面传递过来的流水号
     * @return 比较结果
     */
    boolean checkTradeCode(String userId, String tradeCodeNo);


    /**
     * 删除流水号
     *
     * @param userId 用户 id
     */
    void deleteTradeNo(String userId);

    /**
     * 校验库存
     *
     * @param skuId  sku 编号
     * @param skuNum 下单数量
     * @return 是否有库存
     */
    boolean checkStock(Long skuId, Integer skuNum);

    /**
     * 分页展示订单列表
     *
     * @param pageParam 分页参数
     * @param userId    用户id
     * @return 订单列表
     */
    IPage<OrderInfo> getPage(Page<OrderInfo> pageParam, String userId);

    /**
     * 处理过期订单
     *
     * @param orderId 订单id
     */
    void execExpiredOrder(Long orderId);

    /**
     * 更新过期订单
     *
     * @param orderId 订单id
     * @param flag    更新标记
     */
    void execExpiredOrder(Long orderId, String flag);

    /**
     * 根据订单Id 修改订单的状态
     *
     * @param orderId       订单id
     * @param processStatus 订单状态
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    /**
     * 根据订单id获取订单信息
     *
     * @param orderId 订单id
     * @return 订单信息
     */
    OrderInfo getOrderInfo(Long orderId);

    /**
     * 发送消息给库存！
     *
     * @param orderId 订单id
     */
    void sendOrderStatus(Long orderId);

    /**
     * 将orderInfo变为map集合
     *
     * @param orderInfo 订单信息
     */
    Map initWareOrderToMap(OrderInfo orderInfo);

    /**
     * 订单拆分
     *
     * @param orderId    订单id
     * @param wareSkuMap 库存信息
     * @return 拆分结果
     */
    List<OrderInfo> orderSplit(long orderId, String wareSkuMap);
}
