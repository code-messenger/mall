package cool.yunlong.mall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import cool.yunlong.mall.model.enums.PaymentStatus;
import cool.yunlong.mall.model.enums.PaymentType;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.model.payment.PaymentInfo;
import cool.yunlong.mall.order.client.OrderFeignClient;
import cool.yunlong.mall.payment.config.AlipayConfig;
import cool.yunlong.mall.payment.service.AlipayService;
import cool.yunlong.mall.payment.service.PaymentService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @author yunlong
 * @since 2022/7/1 20:50
 */
@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private AlipayClient alipayClient;

    @Qualifier("cool.yunlong.mall.order.client.OrderFeignClient")
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private PaymentService paymentService;


    /**
     * 根据订单id生成支付二维码
     *
     * @param orderId 订单id
     * @return 支付二维码
     */
    @SneakyThrows
    @Override
    public String createPaymentQRCode(Long orderId) {

        // 获取订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);

        if (orderInfo.getOrderStatus().equals("CLOSED") || orderInfo.getOrderStatus().equals("PAID")) {
            return "订单已关闭或已完成支付，请勿重复支付";
        }

        // 保存订单
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());

        // 创建支付请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 设置异步回调地址
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        // 设置同步回调地址
        request.setReturnUrl(AlipayConfig.return_payment_url);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getId());
        bizContent.put("total_amount", 0.01);
        bizContent.put("subject", orderInfo.getTradeBody());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 设置超时时间
        Calendar calendar = Calendar.getInstance();
        // 设置超时时间为当前时间加上10分钟
        calendar.add(Calendar.MINUTE, 15);
        bizContent.put("time_expire", simpleDateFormat.format(calendar.getTime()));

        request.setBizContent(bizContent.toString());
        // 生成支付二维码
        return alipayClient.pageExecute(request).getBody();
    }

    /**
     * 退款
     *
     * @param orderId 订单id
     * @return 退款结果
     */
    @Override
    public boolean refund(Long orderId) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("refund_reason", "颜色浅了点");
        // out_request_no

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            // 更新交易记录 ： 关闭
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
            paymentService.updatePaymentInfo(orderInfo.getOutTradeNo(), paymentInfo.getPaymentType(), paymentInfo);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 关闭订单
     *
     * @param orderId 订单id
     * @return 关闭结果
     */
    @SneakyThrows
    @Override
    public Boolean closePay(Long orderId) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        HashMap<String, Object> map = new HashMap<>();
        // map.put("trade_no",paymentInfo.getTradeNo()); // 从paymentInfo 中获取！
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("operator_id", "YX01");
        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    /**
     * 查询交易记录
     *
     * @param orderId 订单id
     * @return 交易结果
     */
    @SneakyThrows
    @Override
    public Boolean checkPayment(Long orderId) {
        // 根据订单Id 查询订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        // 根据out_trade_no 查询交易记录
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        return response.isSuccess();
    }


}

