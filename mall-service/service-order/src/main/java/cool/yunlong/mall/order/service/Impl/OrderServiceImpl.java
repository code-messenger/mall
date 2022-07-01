package cool.yunlong.mall.order.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cool.yunlong.mall.cart.client.CartFeignClient;
import cool.yunlong.mall.common.util.AuthContextHolder;
import cool.yunlong.mall.common.util.HttpClientUtil;
import cool.yunlong.mall.model.cart.CartInfo;
import cool.yunlong.mall.model.enums.OrderStatus;
import cool.yunlong.mall.model.enums.ProcessStatus;
import cool.yunlong.mall.model.order.OrderDetail;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.model.user.UserAddress;
import cool.yunlong.mall.mq.constant.MqConst;
import cool.yunlong.mall.mq.service.RabbitService;
import cool.yunlong.mall.order.mapper.OrderDetailMapper;
import cool.yunlong.mall.order.mapper.OrderInfoMapper;
import cool.yunlong.mall.order.service.OrderService;
import cool.yunlong.mall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author yunlong
 * @since 2022/6/28 22:37
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Qualifier("cool.yunlong.mall.user.client.UserFeignClient")
    @Autowired
    private UserFeignClient userFeignClient;

    @Qualifier("cartFeignDegradeClient")
    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Autowired
    private RabbitService rabbitService;

    @Override
    public Map<String, Object> getOrderItem(HttpServletRequest request) {
        // 创建一个 map 对象用于存放返回的数据
        Map<String, Object> result = new HashMap<>();

        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);

        // 获取用户的收货地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressList(userId);

        // 获取购物清单
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);

        // 创建一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();

        // 设置订单明细
        cartCheckedList.forEach(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());

            // 添加到集合
            detailArrayList.add(orderDetail);
        });

        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        // 获取交易流水号
        result.put("tradeNo", getTradeNo(userId));
        result.put("userAddressList", userAddressList);
        result.put("detailArrayList", detailArrayList);
        result.put("totalNum", detailArrayList.size());
        result.put("totalAmount", orderInfo.getTotalAmount());
        return result;
    }

    /**
     * 保存订单
     *
     * @param orderInfo 订单信息
     * @return 订单 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo) {
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        String outTradeNo = "MALL" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setCreateTime(new Date());
        // 定义为1天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());

        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        // 获取订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        StringBuilder tradeBody = new StringBuilder();
        for (OrderDetail orderDetail : orderDetailList) {
            tradeBody.append(orderDetail.getSkuName()).append(" ");
        }
        if (tradeBody.toString().length() > 100) {
            orderInfo.setTradeBody(tradeBody.substring(0, 100));
        } else {
            orderInfo.setTradeBody(tradeBody.toString());
        }

        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }

        //发送延迟队列，如果定时未支付，取消订单
        rabbitService.sendDelayMsg(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL, MqConst.ROUTING_ORDER_CANCEL, orderInfo.getId(), MqConst.DELAY_TIME);
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        // 定义一个流水号
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String redisTradeNo = redisTemplate.opsForValue().get(tradeNoKey);
        return tradeCodeNo.equals(redisTradeNo);
    }

    @Override
    public void deleteTradeNo(String userId) {
        // 定义key
        String tradeNoKey = "user:" + userId + ":tradeCode";
        // 删除数据
        redisTemplate.delete(tradeNoKey);
    }

    @Value("${ware.url}")
    private String WARE_URL;

    /**
     * 校验库存
     *
     * @param skuId  sku 编号
     * @param skuNum 下单数量
     * @return 是否有库存
     */
    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        // 远程调用http://localhost:9001/hasStock?skuId=10221&num=2
        String result = HttpClientUtil.doGet(WARE_URL + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    /**
     * 分页展示订单列表
     *
     * @param pageParam 分页参数
     * @param userId    用户id
     * @return 订单列表
     */
    @Override
    public IPage<OrderInfo> getPage(Page<OrderInfo> pageParam, String userId) {
        // 分页查询
        IPage<OrderInfo> page = orderInfoMapper.selectOrderPage(pageParam, userId);
        // 设置订单状态
        page.getRecords().forEach(orderInfo -> orderInfo.setOrderStatus(OrderStatus.getStatusNameByStatus(orderInfo.getOrderStatus())));
        return page;
    }

    /**
     * 处理过期订单
     *
     * @param orderId 订单id
     */
    @Override
    public void execExpiredOrder(Long orderId) {
        // orderInfo
        updateOrderStatus(orderId, ProcessStatus.CLOSED);
    }

    /**
     * 根据订单id修改订单的状态
     *
     * @param orderId       订单id
     * @param processStatus 订单状态
     */
    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus.name());
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfoMapper.updateById(orderInfo);
    }
}

