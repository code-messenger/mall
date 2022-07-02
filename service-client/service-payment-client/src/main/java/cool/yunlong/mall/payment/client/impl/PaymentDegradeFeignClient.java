package cool.yunlong.mall.payment.client.impl;

import cool.yunlong.mall.model.payment.PaymentInfo;
import cool.yunlong.mall.payment.client.PaymentFeignClient;

/**
 * @author yunlong
 * @since 2022/7/2 18:16
 */
public class PaymentDegradeFeignClient implements PaymentFeignClient {
    @Override
    public Boolean closePay(Long orderId) {
        return null;
    }

    @Override
    public Boolean checkPayment(Long orderId) {
        return null;
    }

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {
        return null;
    }
}
