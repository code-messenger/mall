package cool.yunlong.mall.activity.service.impl;

import cool.yunlong.mall.activity.mapper.SeckillGoodsMapper;
import cool.yunlong.mall.activity.service.SeckillGoodsService;
import cool.yunlong.mall.activity.util.CacheHelper;
import cool.yunlong.mall.common.constant.RedisConst;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.common.result.ResultCodeEnum;
import cool.yunlong.mall.common.util.MD5;
import cool.yunlong.mall.model.activity.OrderRecode;
import cool.yunlong.mall.model.activity.SeckillGoods;
import cool.yunlong.mall.model.order.OrderDetail;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.model.user.UserAddress;
import cool.yunlong.mall.order.client.OrderFeignClient;
import cool.yunlong.mall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yunlong
 * @since 2022/7/2 18:55
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Qualifier("cool.yunlong.mall.user.client.UserFeignClient")
    @Autowired
    private UserFeignClient userFeignClient;

    @Qualifier("cool.yunlong.mall.order.client.OrderFeignClient")
    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 返回全部列表
     *
     * @return 全部列表
     */
    @Override
    public List<SeckillGoods> findAll() {
        return (List<SeckillGoods>) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
    }

    /**
     * 根据ID获取实体
     *
     * @param id 实体ID
     * @return 实体
     */
    @Override
    public SeckillGoods getSeckillGoods(Long id) {
        return (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(id.toString());
    }

    /**
     * 根据用户和商品ID实现秒杀下单
     *
     * @param skuId  商品ID
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void seckillOrder(Long skuId, String userId) {
        // 产品状态位， 1：可以秒杀 0：秒杀结束
        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state)) {
            // 已售罄
            return;
        }

        // 判断用户是否下单
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId, RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        if (!isExist) {
            return;
        }

        // 获取队列中的商品，如果能够获取，则商品存在，可以下单
        String goodsId = (String) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        if (StringUtils.isEmpty(goodsId)) {
            // 更新状态位
            redisTemplate.convertAndSend("seckillpush", skuId + ":0");
            // 已售罄
            return;
        }

        // 订单记录
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setSeckillGoods(this.getSeckillGoods(skuId));
        orderRecode.setNum(1);
        // 生成订单单码
        orderRecode.setOrderStr(MD5.encrypt(userId + skuId));

        // 订单数据存入 Reids
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(), orderRecode);
        // 更新库存
        this.updateStockCount(orderRecode.getSeckillGoods().getSkuId());
    }

    // 表示更新mysql -- redis 的库存数据！
    public void updateStockCount(Long skuId) {
        // 加锁！
        Lock lock = new ReentrantLock();
        // 上锁
        lock.lock();
        try {
            // 获取到存储库存剩余数！
            // key = seckill:stock:46
            String stockKey = RedisConst.SECKILL_STOCK_PREFIX + skuId;
            //  redisTemplate.opsForList().leftPush(key,seckillGoods.getSkuId());
            Long count = redisTemplate.boundListOps(stockKey).size();
            //  减少库存数！方式一减少压力!
            if (count != null && count % 2 == 0) {
                // 开始更新数据！
                SeckillGoods seckillGoods = this.getSeckillGoods(skuId);
                // 赋值剩余库存数！
                seckillGoods.setStockCount(count.intValue());
                // 更新时间
                seckillGoods.setUpdateTime(new Date());
                // 更新的数据库！
                seckillGoodsMapper.updateById(seckillGoods);
                // 更新缓存！
                redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(seckillGoods.getSkuId().toString(), seckillGoods);
            }
        } finally {
            // 解锁！
            lock.unlock();
        }
    }

    /**
     * 检查订单状态
     *
     * @param userId 用户ID
     * @param skuId  商品ID
     */
    @Override
    public Result checkOrder(String userId, Long skuId) {
        // 判断用户是否存在
        Boolean isUserExist = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        if (Boolean.TRUE.equals(isUserExist)) {
            // 判断用户是否抢购成功
            Boolean isGrabOrder = redisTemplate.opsForHash().hasKey(RedisConst.SECKILL_ORDERS, userId);
            if (Boolean.TRUE.equals(isGrabOrder)) {
                // 获取订单信息
                OrderRecode orderRecode = (OrderRecode) redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS, userId);
                if (orderRecode != null && orderRecode.getSeckillGoods().getSkuId().equals(skuId)) {
                    // 返回，去下单
                    return Result.build(orderRecode, ResultCodeEnum.SUCCESS);
                }
            }
        }
        // 判断用户是否真正下单
        Boolean isRealOrder = redisTemplate.opsForHash().hasKey(RedisConst.SECKILL_ORDERS_USERS, userId);
        if (Boolean.TRUE.equals(isRealOrder)) {
            // 获取订单信息
            String orderId = (String) redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS_USERS, userId);
            // 返回我的订单
            return Result.build(orderId, ResultCodeEnum.SUCCESS);
        }
        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state)) {
            // 已售罄 抢单失败
            return Result.build(null, ResultCodeEnum.SECKILL_FAIL);
        }
        // 正在排队中
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    @Override
    public Result getResult(String userId) {
        // 先得到用户想要购买的商品！
        OrderRecode orderRecode = (OrderRecode) redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS, userId);
        if (null == orderRecode) {
            return Result.fail().message("非法操作");
        }
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

        // 获取用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressList(userId);

        // 声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        // 添加到集合
        detailArrayList.add(orderDetail);

        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", userAddressList);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalAmount", orderInfo.getTotalAmount());
        return Result.ok(result);
    }

    @Override
    public Result getOrderId(OrderInfo orderInfo, String userId) {
        orderInfo.setUserId(Long.parseLong(userId));

        Long orderId = orderFeignClient.submitOrder(orderInfo);
        if (null == orderId) {
            return Result.fail().message("下单失败，请重新操作");
        }

        // 下单记录
        redisTemplate.opsForHash().put(RedisConst.SECKILL_ORDERS_USERS, userId, orderId.toString());

        //删除下单信息
        redisTemplate.opsForHash().delete(RedisConst.SECKILL_ORDERS, userId);

        return Result.ok(orderId);
    }
}
