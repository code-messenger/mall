package cool.yunlong.mall.activity.controller;

import cool.yunlong.mall.activity.service.SeckillGoodsService;
import cool.yunlong.mall.activity.util.CacheHelper;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.common.result.ResultCodeEnum;
import cool.yunlong.mall.common.util.AuthContextHolder;
import cool.yunlong.mall.common.util.DateUtil;
import cool.yunlong.mall.common.util.MD5;
import cool.yunlong.mall.model.activity.SeckillGoods;
import cool.yunlong.mall.model.activity.UserRecode;
import cool.yunlong.mall.model.order.OrderInfo;
import cool.yunlong.mall.mq.constant.MqConst;
import cool.yunlong.mall.mq.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author yunlong
 * @since 2022/7/2 18:57
 */

@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsApiController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private RabbitService rabbitService;


    /**
     * 返回全部列表
     *
     * @return 全部列表
     */
    @GetMapping("/findAll")
    public Result<List<SeckillGoods>> findAll() {
        return Result.ok(seckillGoodsService.findAll());
    }

    /**
     * 获取实体
     *
     * @param skuId 商品id
     * @return 商品信息
     */
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result<SeckillGoods> getSeckillGoods(@PathVariable("skuId") Long skuId) {
        return Result.ok(seckillGoodsService.getSeckillGoods(skuId));
    }

    /**
     * 获取下单码
     *
     * @param skuId   商品id
     * @param request 当前请求
     * @return Result  返回结果
     */
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        if (seckillGoods == null) {
            return Result.fail().message("商品不存在");
        }
        // 判断是否在秒杀时间内
        Date curTime = new Date();
        if (DateUtil.dateCompare(seckillGoods.getStartTime(), curTime) && DateUtil.dateCompare(curTime, seckillGoods.getEndTime())) {
            // 将userId进行MD5加密
            String skuIdStr = MD5.encrypt(userId);
            return Result.ok(skuIdStr);
        }
        return Result.fail().message("获取下单码失败");
    }

    /**
     * 秒杀下单
     */
    @PostMapping("/auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        // 获取用户id
        String userId = AuthContextHolder.getUserId(request);
        // 获取skuIdStr
        String skuIdStr = request.getParameter("skuIdStr");
        // 校验下单码
        if (!MD5.encrypt(userId).equals(skuIdStr)) {
            return Result.fail().message("下单码错误");
        }
        String state = (String) CacheHelper.get(skuId.toString());
        // 校验状态值
        if (StringUtils.isEmpty(state)) {
            //请求不合法
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        if ("1".equals(state)) {
            //用户记录
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);

            rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_SECKILL_USER, MqConst.ROUTING_SECKILL_USER, userRecode);
        } else {
            //已售罄
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }
        return Result.ok();
    }

    /**
     * 检查订单状态
     *
     * @param skuId   商品id
     * @param request 当前请求
     * @return Result  返回结果
     */
    @GetMapping("/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId, HttpServletRequest request) {
        // 获取用户id
        String userId = AuthContextHolder.getUserId(request);
        return seckillGoodsService.checkOrder(userId, skuId);
    }

    /**
     * 秒杀下单
     */
    @GetMapping("/auth/trade")
    public Result trade(HttpServletRequest request) {
        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);

        return seckillGoodsService.getResult(userId);
    }

    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);

        return seckillGoodsService.getOrderId(orderInfo, userId);
    }

}