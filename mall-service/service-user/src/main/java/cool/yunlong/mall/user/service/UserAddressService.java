package cool.yunlong.mall.user.service;

import cool.yunlong.mall.model.user.UserAddress;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 21:50
 */
public interface UserAddressService {

    /**
     * 根据用户 id 查询用户收货地址列表
     *
     * @param userId 用户 id
     * @return 用户收货地址列表
     */
    List<UserAddress> findUserAddressListByUserId(String userId);
}
