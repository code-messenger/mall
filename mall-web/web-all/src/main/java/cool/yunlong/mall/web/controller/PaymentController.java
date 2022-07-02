package cool.yunlong.mall.web.controller;

import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yunlong
 * @since 2022/7/1 20:12
 */
@Controller
public class PaymentController {

    @Qualifier("cool.yunlong.mall.order.client.OrderFeignClient")
    @Autowired
    private OrderFeignClient orderFeignClient;


    @GetMapping("/pay.html")
    public String pay(HttpServletRequest request) {
        // 获取订单id
        String orderId = request.getParameter("orderId");

        // 远程调用订单服务
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));

        // 保存数据
        request.setAttribute("orderInfo", orderInfo);

        // 返回视图
        return "payment/pay";
    }

    // 同步回调地址
    @GetMapping("/pay/success.html")
    public String paySuccess() {
        // 展示支付成功回调
        return "payment/success";
    }
}
