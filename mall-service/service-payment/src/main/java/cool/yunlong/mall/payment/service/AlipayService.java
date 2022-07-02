package cool.yunlong.mall.payment.service;

/**
 * @author yunlong
 * @since 2022/7/1 20:47
 */
public interface AlipayService {

    /**
     * 根据订单id生成支付二维码
     *
     * @param orderId 订单id
     * @return 支付二维码
     */
    String createPaymentQRCode(Long orderId);

    /**
     * 退款
     *
     * @param orderId 订单id
     * @return 退款结果
     */
    boolean refund(Long orderId);

    /**
     * 关闭订单
     *
     * @param orderId 订单id
     * @return 关闭结果
     */
    Boolean closePay(Long orderId);

    /**
     * 查询交易记录
     *
     * @param orderId 订单id
     * @return 交易结果
     */
    Boolean checkPayment(Long orderId);
}
