package cool.yunlong.mall.payment.service;

import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/7/1 20:19
 */
public interface PaymentService {

    /**
     * 保存交易记录
     *
     * @param orderInfo 订单信息
     */
    void savePaymentInfo(OrderInfo orderInfo, String payType);

    /**
     * 根据订单id获取交易记录
     *
     * @param outTradeNo  订单id
     * @param paymentType 交易类型
     * @return 交易记录
     */
    PaymentInfo getPaymentInfo(String outTradeNo, String paymentType);

    /**
     * 支付成功
     *
     * @param outTradeNo  流水号
     * @param paymentType 交易类型
     * @param paramsMap   交易参数
     */
    void paySuccess(String outTradeNo, String paymentType, Map<String, String> paramsMap);

    /**
     * 更新交易记录
     *
     * @param outTradeNo  流水号
     * @param paymentType 支付方式
     * @param paymentInfo 交易记录
     */
    void updatePaymentInfo(String outTradeNo, String paymentType, PaymentInfo paymentInfo);

    /**
     * 关闭订单
     *
     * @param orderId 订单id
     */
    void closePayment(Long orderId);
}
