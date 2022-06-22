package cool.yunlong.mall.web.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 前台商品详情页面控制器
 *
 * @author yunlong
 * @since 2022/6/18 12:24
 */
@Controller
public class ItemController {

    @Qualifier("cool.yunlong.mall.item.client.ItemFeignClient")
    @Autowired
    private ItemFeignClient itemFeignClient;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {
        // 调用商品服务查询商品详情
        Result<Map<String, Object>> item = itemFeignClient.getItem(skuId);

        // 将商品详情数据放入model
        model.addAllAttributes(item.getData());

        // 商品详情页面
        return "item/item";
    }
}
