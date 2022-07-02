package cool.yunlong.mall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.enums.PaymentType;
import cool.yunlong.mall.model.payment.PaymentInfo;
import cool.yunlong.mall.payment.config.AlipayConfig;
import cool.yunlong.mall.payment.service.AlipayService;
import cool.yunlong.mall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yunlong
 * @since 2022/7/1 21:14
 */
@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/submit/{orderId}")
    public String submit(@PathVariable Long orderId) {
        return alipayService.createPaymentQRCode(orderId);
    }

    // 支付同步回调       http://api.mall.com/api/payment/alipay/callback/return?
    @GetMapping("/callback/return")
    public String callbackReturn() {
        return "redirect:" + AlipayConfig.return_order_url;
    }

    // 支付异步回调       http://api.mall.com/api/payment/alipay/callback/notify?
    @PostMapping("/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String, String> paramsMap) {
        System.out.println("你回来了.....");
        // Map<String, String> paramsMap = ... // 将异步通知中收到的所有参数都存放到map中
        boolean signVerified = false;
        try {
            //调用SDK验证签名
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //  获取异步通知的参数中的订单号！
        String outTradeNo = paramsMap.get("out_trade_no");
        //  获取异步通知的参数中的订单总金额！
        String totalAmount = paramsMap.get("total_amount");
        //  获取异步通知的参数中的appId！
        String appId = paramsMap.get("app_id");
        //  获取异步通知的参数中的交易状态！
        String tradeStatus = paramsMap.get("trade_status");
        //  根据 outTradeNo 查询数据！
        PaymentInfo paymentinfo = this.paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
        //  保证异步通知的幂等性！notify_id
        String notifyId = paramsMap.get("notify_id");

        //  true:
        if (signVerified) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            if (paymentinfo == null || new BigDecimal("0.01").compareTo(new BigDecimal(totalAmount)) != 0
                    || !appId.equals("app_Id")) {
                return "failure";
            }
            //  放入redis！ setnx：当 key 不存在的时候生效！
            Boolean flag = this.redisTemplate.opsForValue().setIfAbsent(notifyId, notifyId, 1462, TimeUnit.MINUTES);
            //  说明已经处理过了！
            if (Boolean.FALSE.equals(flag)) {
                return "failure";
            }
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                //  修改交易记录状态！再订单状态！
                this.paymentService.paySuccess(outTradeNo, PaymentType.ALIPAY.name(), paramsMap);
                //  this.paymentService.paySuccess(paymentinfo.getId(),paramsMap);
                return "success";
            }
        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }

    // 退款
    @RequestMapping("/refund/{orderId}")
    @ResponseBody
    public Result<Boolean> refund(@PathVariable(value = "orderId") Long orderId) {
        // 调用退款接口
        boolean flag = alipayService.refund(orderId);

        return Result.ok(flag);
    }

    // 关闭支付宝订单     http://localhost:8205/api/payment/alipay/closePay/25
    @GetMapping("/closePay/{orderId}")
    @ResponseBody
    public Boolean closePay(@PathVariable Long orderId) {
        return alipayService.closePay(orderId);
    }

    // 查询交易记录
    @RequestMapping("checkPayment/{orderId}")
    @ResponseBody
    public Boolean checkPayment(@PathVariable Long orderId) {
        // 调用退款接口
        return alipayService.checkPayment(orderId);
    }

    @GetMapping("/getPaymentInfo/{outTradeNo}")
    @ResponseBody
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo) {
        return paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
    }
}




