package cool.yunlong.mall.web.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/28 22:47
 */
@Controller
public class OrderController {

    @Qualifier("cool.yunlong.mall.order.client.OrderFeignClient")
    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("trade.html")
    public String trade(Model model) {
        // 远程调用订单服务
        Result<Map> result = orderFeignClient.trade();
        model.addAllAttributes(result.getData());
        // 返回视图
        return "order/trade";
    }

    @GetMapping("myOrder.html")
    public String myOrder() {
        return "order/myOrder";
    }
}
