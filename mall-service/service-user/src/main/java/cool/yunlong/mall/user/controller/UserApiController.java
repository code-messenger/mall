package cool.yunlong.mall.user.controller;

import cool.yunlong.mall.model.user.UserAddress;
import cool.yunlong.mall.user.service.UserAddressService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 21:46
 */
@Api(tags = "用户数据接口")
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserAddressService userAddressService;

    @Operation(summary = "获取用户收货地址列表", description = "根据用户 id 查询用户收货地址列表")
    @GetMapping("/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressList(@PathVariable String userId) {
        return userAddressService.findUserAddressListByUserId(userId);
    }
}
