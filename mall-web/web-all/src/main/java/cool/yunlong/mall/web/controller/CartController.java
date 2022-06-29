package cool.yunlong.mall.web.controller;

import cool.yunlong.mall.model.product.SkuInfo;
import cool.yunlong.mall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;


@Controller
public class CartController {

    @Qualifier("cool.yunlong.mall.product.client.ProductFeignClient")
    @Autowired
    private ProductFeignClient productFeignClient;

    //  添加购物车
    //  http://cart.gmall.com/addCart.html?skuId=24&skuNum=1&sourceType=query
    @GetMapping("addCart.html")
    public String addToCart(HttpServletRequest request) {
        //  获取skuId ,获取skuNum
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        //  获取到skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
        //  保存数据
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);

        //  返回购物车页面
        return "cart/addCart";
    }

    //  查看购物车列表
    //  cart.html
    @GetMapping("cart.html")
    public String cartList() {

        //  返回购物车列表页面
        return "cart/index";
    }
}