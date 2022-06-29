package cool.yunlong.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cool.yunlong.mall.model.user.UserAddress;
import cool.yunlong.mall.user.mapper.UserAddressMapper;
import cool.yunlong.mall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/28 21:50
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 根据用户 id 查询用户收货地址列表
     *
     * @param userId 用户 id
     * @return 用户收货地址列表
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        // 创建一个queryWrapper对象
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        // 构造条件
        queryWrapper.eq("userId", userId);
        // 查询并返回数据
        return userAddressMapper.selectList(queryWrapper);
    }
}
