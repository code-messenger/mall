package cool.yunlong.mall.cart.controller;

import cool.yunlong.mall.cart.service.CartService;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.common.util.AuthContextHolder;
import cool.yunlong.mall.model.cart.CartInfo;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 17:56
 */
@Api(tags = "购物车接口")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "添加购物车", description = "根据skuId、skuNum添加购物车")
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result<Void> addToCart(@PathVariable("skuId") Long skuId,
                                     @PathVariable("skuNum") Integer skuNum,
                                     HttpServletRequest request) {
        // 获取请求头中的 userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            // 获取临时用户 id
            userId = AuthContextHolder.getUserTempId(request);
        }
        // 添加购物车
        cartService.addToCart(skuId, userId, skuNum);
        // 返回数据
        return Result.ok();
    }

    @Operation(summary = "获取购物车列表", description = "根据用户id查询购物车列表")
    @GetMapping("/cartList")
    public Result<List<CartInfo>> getCartList(HttpServletRequest request) {
        // 获取用户 id
        String userId = AuthContextHolder.getUserId(request);
        // 获取临时用户 id
        String userTempId = AuthContextHolder.getUserTempId(request);
        // 查询购物车列表
        List<CartInfo> cartInfoList = cartService.getCartList(userId, userTempId);
        // 返回数据
        return Result.ok(cartInfoList);
    }

    @Operation(summary = "删除购物车", description = "根据skuId删除购物车")
    @DeleteMapping("/deleteCart/{skuId}")
    public Result<Void> deleteCart(@PathVariable("skuId") Long skuId,
                                      HttpServletRequest request) {
        // 获取请求头中的 userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            // 获取请求头中的临时用户 id
            userId = AuthContextHolder.getUserTempId(request);
        }
        // 删除购物车
        cartService.deleteCart(skuId, userId);
        // 返回数据
        return Result.ok();
    }

    @Operation(summary = "选中状态")
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result<Void> checkCart(@PathVariable Long skuId,
                                     @PathVariable Integer isChecked,
                                     HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        //  判断
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法
        cartService.checkCart(skuId, userId, isChecked);
        return Result.ok();
    }

    @Operation(summary = "获取选中状态为 1 的购物车商品集合")
    @GetMapping("/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId) {
        //  调用服务层方法 ，并返回数据
        return this.cartService.getCartCheckedList(userId);
    }
}

