package cool.yunlong.mall.web.controller;

import cool.yunlong.mall.activity.client.ActivityFeignClient;
import cool.yunlong.mall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/7/2 19:00
 */
@Controller
@RequestMapping("/api/activity")
public class SeckillController {

    @Qualifier("cool.yunlong.mall.activity.client.ActivityFeignClient")
    @Autowired
    private ActivityFeignClient activityFeignClient;

    /**
     * 秒杀列表
     *
     * @param model 模型
     * @return 视图
     */
    @GetMapping("seckill.html")
    public String index(Model model) {
        Result result = activityFeignClient.findAll();
        model.addAttribute("list", result.getData());
        return "seckill/index";
    }

    @GetMapping("seckill/{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) {
        // 通过skuId 查询skuInfo
        Result result = activityFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item", result.getData());
        return "seckill/item";
    }

    /**
     * 排队页面
     *
     * @param skuId    商品id
     * @param skuIdStr 商品id的加密字符串
     * @param request  请求对象
     * @return 视图
     */
    @GetMapping("seckill/queue.html")
    public String queue(@RequestParam(name = "skuId") Long skuId,
                        @RequestParam(name = "skuIdStr") String skuIdStr,
                        HttpServletRequest request) {
        request.setAttribute("skuId", skuId);
        request.setAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";
    }

    /**
     * 确认订单页面
     *
     * @param model 模型
     * @return 视图
     */
    @GetMapping("seckill/trade.html")
    public String trade(Model model) {
        Result<Map<String, Object>> result = activityFeignClient.trade();
        if (result.isOk()) {
            model.addAllAttributes(result.getData());
            return "seckill/trade";
        } else {
            model.addAttribute("message", result.getMessage());

            return "seckill/fail";
        }
    }
}