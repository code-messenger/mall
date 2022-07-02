package cool.yunlong.mall.payment.client;

import cool.yunlong.mall.model.payment.PaymentInfo;
import cool.yunlong.mall.payment.client.impl.PaymentDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yunlong
 * @since 2022/7/2 18:16
 */
@FeignClient(value = "service-payment", fallback = PaymentDegradeFeignClient.class)
public interface PaymentFeignClient {

    @GetMapping("/api/payment/alipay/closePay/{orderId}")
    Boolean closePay(@PathVariable Long orderId);

    @GetMapping("/api/payment/alipay/checkPayment/{orderId}")
    Boolean checkPayment(@PathVariable Long orderId);

    @GetMapping("/api/payment/alipay/getPaymentInfo/{outTradeNo}")
    PaymentInfo getPaymentInfo(@PathVariable String outTradeNo);
}
