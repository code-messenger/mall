package cool.yunlong.mall.order.controller;

import com.alibaba.fastjson.JSON;
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
import cool.yunlong.mall.model.user.UserAddress;
import cool.yunlong.mall.order.service.OrderService;
import cool.yunlong.mall.product.client.ProductFeignClient;
import cool.yunlong.mall.user.client.UserFeignClient;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author yunlong
 * @since 2022/6/28 22:20
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order")
public class OrderApiController {


    @Qualifier("cool.yunlong.mall.cart.client.CartFeignClient")
    @Autowired
    private CartFeignClient cartFeignClient;

    @Qualifier("cool.yunlong.mall.user.client.UserFeignClient")
    @Autowired
    private UserFeignClient userFeignClient;

    @Qualifier("cool.yunlong.mall.product.client.ProductFeignClient")
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Operation(summary = "下单")
    @GetMapping("/auth/trade")
    public Result trade(HttpServletRequest request) {
        //  获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        //   userAddressList，detailArrayList，totalNum，totalAmount 以map 形式存储！
        //  声明一个map 集合
        HashMap<String, Object> map = new HashMap<>();
        //  获取用户收货地址类别
        List<UserAddress> userAddressList = this.userFeignClient.findUserAddressList(userId);

        //  获取送货清单数据
        List<CartInfo> cartCheckedList = this.cartFeignClient.getCartCheckedList(userId);

        //  声明一个变量来存储商品的总件数
        AtomicInteger totalNum = new AtomicInteger();
        //  需要将数据转换为OrderDetail 集合
        List<OrderDetail> orderDetailArrayList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            //  计算总件数
            totalNum.addAndGet(orderDetail.getSkuNum());
            return orderDetail;
        }).collect(Collectors.toList());

        OrderInfo orderInfo = new OrderInfo();
        //  一定要赋值订单明细集合
        orderInfo.setOrderDetailList(orderDetailArrayList);
        //  调用计算总金额方法.
        orderInfo.sumTotalAmount();
        //  存储数据
        map.put("userAddressList", userAddressList);
        //  不能直接存储 cartCheckedList集合，因为页面渲染的 orderPrice 字段，在cartInfo 中不存在！
        map.put("detailArrayList", orderDetailArrayList);
        //  保存总件数：
        map.put("totalNum", totalNum);
        //  计算总金额： 虽然页面提交了总金额的数据，但是不能在直接使用！ 为啥呢？因为页面可以修改总金额，不安全。因此在后台要重新计算一下！
        map.put("totalAmount", orderInfo.getTotalAmount());
        //  存一个流水号 ${tradeNo}
        //  调用方法获取流水号
        String tradeNo = this.orderService.getTradeNo(userId);
        map.put("tradeNo", tradeNo);
        //  返回数据
        return Result.ok(map);
    }

    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        //  获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        //  赋值用户Id
        orderInfo.setUserId(Long.parseLong(userId));
        //  获取页面传递过来的流水号
        String tradeNo = request.getParameter("tradeNo");
        //  调用比较方法
        boolean result = this.orderService.checkTradeCode(userId, tradeNo);
        if (!result) {
            //  不能提交订单，并提示信息
            return Result.fail().message("不能无刷新重复提交订单");
        }

        //  创建一个集合对象
        ArrayList<CompletableFuture<Void>> futureList = new ArrayList<>();
        //  创建一个信息提示集合
        ArrayList<String> errorList = new ArrayList<>();

        //  调用校验库存  23,24
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            //  创建一个CompletableFuture
            CompletableFuture<Void> checkCompletableFuture = CompletableFuture.runAsync(() -> {
                //  调用校验库存方法
                boolean exist = this.orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                //  判断
                if (!exist) {
                    //  说明没有库存了。
                    errorList.add(orderDetail.getSkuName() + "没有足够的库存");
                }
            }, threadPoolExecutor);

            //  添加到集合中
            futureList.add(checkCompletableFuture);

            CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                //  每个商品都要验证
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                //  商品的最新价格
                BigDecimal skuPrice = this.productFeignClient.getSkuPrice(orderDetail.getSkuId());
                //  比较价格是否一致  return xs != ys ? ((xs > ys) ? 1 : -1) : 0; //0 -1 都是涨价
                String msg = orderPrice.compareTo(skuPrice) == 1 ? "降价" : "涨价";
                if (orderPrice.compareTo(skuPrice) != 0) {
                    //  说明价格有变动
                    //  计算价格变动
                    BigDecimal price = orderPrice.subtract(skuPrice).abs();
                    //  如果价格有变动，那么可以将最新价格同步到购物车.
                    String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
                    //  hget key field;
                    CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, orderDetail.getSkuId().toString());
                    cartInfo.setSkuPrice(skuPrice);
                    //  hset key field value
                    this.redisTemplate.opsForHash().put(cartKey, orderDetail.getSkuId().toString(), cartInfo);
                    //  如果有错误消息，则将数据添加到集合中
                    errorList.add(orderDetail.getSkuName() + msg + price);
                }
            }, threadPoolExecutor);
            //  添加到集合中
            futureList.add(priceCompletableFuture);
        }
        //  将多线程执行的结果进行多任务组合
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        //  判断当前是否需要显示提示信息
        if (errorList.size() > 0) {
            //  需要提示
            return Result.fail().message(StringUtils.join(errorList, ","));
        }
        //  调用服务层方法   Long orderId = this.orderService.saveOrderInfo(orderInfo,request);
        Long orderId = this.orderService.saveOrderInfo(orderInfo);

        //  删除缓存的流水号
        this.orderService.deleteTradeNo(userId);
        //  返回数据
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

    @Operation(summary = "获取订单信息", description = "根据订单id获取订单信息")
    @GetMapping("/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId) {
        return orderService.getOrderInfo(orderId);
    }

    @Operation(summary = "订单拆分")
    @GetMapping("/orderSplit")
    public String orderSplit(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");

        // 拆单：获取到的子订单集合
        List<OrderInfo> subOrderInfoList = orderService.orderSplit(Long.parseLong(orderId), wareSkuMap);
        // 声明一个存储map的集合
        ArrayList<Map> mapArrayList = new ArrayList<>();
        // 生成子订单集合
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrderToMap(orderInfo);
            // 添加到集合中！
            mapArrayList.add(map);
        }
        return JSON.toJSONString(mapArrayList);
    }
}

