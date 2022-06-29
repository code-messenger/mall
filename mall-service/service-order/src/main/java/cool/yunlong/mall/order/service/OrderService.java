package cool.yunlong.mall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cool.yunlong.mall.model.order.OrderInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/28 22:37
 */
public interface OrderService extends IService<OrderInfo> {

    /**
     * 获取订单明细
     *
     * @param request 请求域对象
     * @return 订单明细
     */
    Map<String, Object> getOrderItem(HttpServletRequest request);

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
}
