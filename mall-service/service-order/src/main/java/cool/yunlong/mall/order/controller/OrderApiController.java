package cool.yunlong.mall.order.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.cart.client.CartFeignClient;
import cool.yunlong.mall.common.constant.RedisConst;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.common.util.AuthContextHolder;
import cool.yunlong.mall.model.cart.CartInfo;
import cool.yunlong.mall.model.order.OrderDetail;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.order.service.OrderService;
import cool.yunlong.mall.product.client.ProductFeignClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yunlong
 * @since 2022/6/28 22:20
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Qualifier("cool.yunlong.mall.product.client.ProductFeignClient")
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Qualifier("cool.yunlong.mall.cart.client.CartFeignClient")
    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private OrderService orderService;

    @Operation(summary = "下单")
    @GetMapping("/auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request) {
        // 获取订单明细
        Map<String, Object> result = orderService.getOrderItem(request);

        // 返回数据
        return Result.ok(result);
    }

    @Operation(summary = "提交订单")
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        // 获取前台页面的流水号
        String tradeNo = request.getParameter("tradeNo");

        // 调用服务层的比较方法
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag) {
            // 比较失败！
            return Result.fail().message("不能重复提交订单！");
        }

        //  删除流水号
        orderService.deleteTradeNo(userId);
        List<String> errorList = new ArrayList<>();
        List<CompletableFuture> futureList = new ArrayList<>();
        // 验证库存：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            CompletableFuture<Void> checkStockCompletableFuture = CompletableFuture.runAsync(() -> {
                // 验证库存：
                boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!result) {
                    errorList.add(orderDetail.getSkuName() + "库存不足！");
                }
            }, threadPoolExecutor);
            futureList.add(checkStockCompletableFuture);

            CompletableFuture<Void> checkPriceCompletableFuture = CompletableFuture.runAsync(() -> {
                // 验证价格：
                BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                    // 重新查询价格！
                    List<CartInfo> cartCheckedList = this.cartFeignClient.getCartCheckedList(userId);
                    //  写入缓存：
                    cartCheckedList.forEach(cartInfo -> this.redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX, cartInfo.getSkuId().toString(), cartInfo));
                    errorList.add(orderDetail.getSkuName() + "价格有变动！");
                }
            }, threadPoolExecutor);
            futureList.add(checkPriceCompletableFuture);
        }

        //合并线程
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        if (errorList.size() > 0) {
            return Result.fail().message(StringUtils.join(errorList, ","));
        }
        // 验证通过，保存订单！
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }

    @Operation(summary = "我的订单", description = "分页展示订单列表")
    @GetMapping("/auth/{page}/{limit}")
    public Result<IPage<OrderInfo>> index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 封装分页对象
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        // 分页查询我的订单
        IPage<OrderInfo> pageModel = orderService.getPage(pageParam, userId);
        return Result.ok(pageModel);
    }
}
