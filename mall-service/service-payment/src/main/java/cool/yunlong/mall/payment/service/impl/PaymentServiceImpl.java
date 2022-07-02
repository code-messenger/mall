package cool.yunlong.mall.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cool.yunlong.mall.model.enums.PaymentStatus;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.model.payment.PaymentInfo;
import cool.yunlong.mall.mq.constant.MqConst;
import cool.yunlong.mall.mq.service.RabbitService;
import cool.yunlong.mall.payment.mapper.PaymentInfoMapper;
import cool.yunlong.mall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/7/1 20:21
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String payType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", payType);
        PaymentInfo paymentInfoQuery = paymentInfoMapper.selectOne(queryWrapper);

        if (paymentInfoQuery != null) {
            return;
        }

        // 创建paymentInfo对象
        PaymentInfo paymentInfo = new PaymentInfo();
        // 设置paymentInfo的属性
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setUserId(orderInfo.getUserId());
        paymentInfo.setPaymentType(payType);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setCreateTime(new Date());
        paymentInfoMapper.insert(paymentInfo);
    }

    /**
     * 根据订单id获取交易记录
     *
     * @param outTradeNo  订单id
     * @param paymentType 交易类型
     * @return 交易记录
     */
    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo, String paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        queryWrapper.eq("payment_type", paymentType);
        return paymentInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 支付成功
     *
     * @param outTradeNo  流水号
     * @param paymentType 交易类型
     * @param paramsMap   交易参数
     */
    @Override
    public void paySuccess(String outTradeNo, String paymentType, Map<String, String> paramsMap) {
        PaymentInfo paymentInfoQuery = this.getPaymentInfo(outTradeNo, paymentType);
        if (paymentInfoQuery == null) {
            return;
        }
        try {
            //  改造一下更新的方法！
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            paymentInfo.setCallbackContent(paramsMap.toString());
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            //  查询条件也可以作为更新条件！
            this.updatePaymentInfo(outTradeNo, paymentType, paymentInfo);
        } catch (Exception e) {
            //  删除key
            this.redisTemplate.delete(paramsMap.get("notify_id"));
            e.printStackTrace();
        }
        // 发送通知，更新订单状态
        this.rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY, MqConst.ROUTING_PAYMENT_PAY, paymentInfoQuery.getOrderId());
    }

    /**
     * 更新交易记录
     *
     * @param outTradeNo  流水号
     * @param paymentType 交易类型
     * @param paymentInfo 交易记录
     */
    public void updatePaymentInfo(String outTradeNo, String paymentType, PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        queryWrapper.eq("payment_type", paymentType);
        paymentInfoMapper.update(paymentInfo, queryWrapper);
    }

    /**
     * 关闭订单
     *
     * @param orderId 订单id
     */
    @Override
    public void closePayment(Long orderId) {
        // 设置关闭交易记录的条件  118
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("order_id", orderId);
        // 如果当前的交易记录不存在，则不更新交易记录
        Integer count = paymentInfoMapper.selectCount(paymentInfoQueryWrapper);
        if (null == count || count == 0) return;
        // 在关闭支付宝交易之前。还需要关闭paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
        paymentInfoMapper.update(paymentInfo, paymentInfoQueryWrapper);
    }

}

