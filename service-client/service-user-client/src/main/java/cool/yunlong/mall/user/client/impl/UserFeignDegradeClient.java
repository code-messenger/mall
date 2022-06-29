package cool.yunlong.mall.user.client.impl;

import cool.yunlong.mall.model.user.UserAddress;
import cool.yunlong.mall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 22:01
 */
@Component
public class UserFeignDegradeClient implements UserFeignClient {
    /**
     * 根据用户 id 查询用户收货地址列表
     *
     * @param userId 用户 id
     * @return 用户收货地址列表
     */
    @Override
    public List<UserAddress> findUserAddressList(String userId) {
        return null;
    }
}
