package cool.yunlong.mall.user.service;

import cool.yunlong.mall.model.user.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/27 21:50
 */
public interface UserService {
    /**
     * 用户登录
     *
     * @param loginUser 用户信息
     * @return 查询的用户信息
     */
    Map login(UserInfo loginUser, HttpServletRequest httpServletRequest);

    void logout(HttpServletRequest request);
}
