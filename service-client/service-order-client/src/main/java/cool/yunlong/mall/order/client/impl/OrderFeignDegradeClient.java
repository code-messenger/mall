package cool.yunlong.mall.order.client.impl;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/28 22:43
 */
@Component
public class OrderFeignDegradeClient implements OrderFeignClient {
    @Override
    public Result<Map<String, Object>> trade(HttpServletRequest request) {
        return null;
    }
}
