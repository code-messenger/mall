package cool.yunlong.mall.user.client;

import cool.yunlong.mall.model.user.UserAddress;
import cool.yunlong.mall.user.client.impl.UserFeignDegradeClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 22:00
 */
@FeignClient(value = "service-user", fallback = UserFeignDegradeClient.class)
public interface UserFeignClient {

    /**
     * 根据用户 id 查询用户收货地址列表
     *
     * @param userId 用户 id
     * @return 用户收货地址列表
     */
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressList(@PathVariable String userId);
}
