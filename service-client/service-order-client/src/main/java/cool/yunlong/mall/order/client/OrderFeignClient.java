package cool.yunlong.mall.order.client;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.order.client.impl.OrderFeignDegradeClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/28 22:42
 */
@FeignClient(value = "service-order", fallback = OrderFeignDegradeClient.class)
public interface OrderFeignClient {

    @GetMapping("/api/order/auth/trade")
    Result<Map<String, Object>> trade(HttpServletRequest request);
}
