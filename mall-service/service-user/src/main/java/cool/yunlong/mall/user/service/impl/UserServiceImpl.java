package cool.yunlong.mall.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cool.yunlong.mall.common.constant.RedisConst;
import cool.yunlong.mall.common.util.IpUtil;
import cool.yunlong.mall.model.user.UserInfo;
import cool.yunlong.mall.user.mapper.UserInfoMapper;
import cool.yunlong.mall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yunlong
 * @since 2022/6/27 21:51
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用户登录
     *
     * @param loginUser 用户信息
     * @return 查询的用户信息
     */
    @Override
    public Map login(UserInfo loginUser, HttpServletRequest httpServletRequest) {
        // 创建一个 map 用于存储返回结果
        HashMap<String, Object> map = new HashMap<>();

        // 创建queryWrapper对象
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", loginUser.getLoginName());
        // MD5加密   要不无法与数据库比对
        String encoderPassword = DigestUtils.md5DigestAsHex(loginUser.getPasswd().getBytes());
        queryWrapper.eq("passwd", encoderPassword);
        // 根据登录用户比对数据库中的用户
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        if (userInfo != null) {
            // 用户存在
            String token = UUID.randomUUID().toString();
            map.put("token", token);
            map.put("nickName", userInfo.getNickName());

            // 将用户信息放入缓存
            String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;

            // 防止ip地址被盗用，存储一个ip地址
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", userInfo.getId().toString());
            // 从缓存中获取 userId 时，判断存入时的 IP 地址 与当前请求的 IP 地址是否一致，如果一致，则返回userId，否则，返回非法登录
            jsonObject.put("ip", IpUtil.getIpAddress(httpServletRequest));
            redisTemplate.opsForValue().set(loginKey, jsonObject.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            // 返回数据
            return map;
        } else {
            return null;
        }
    }

    /**
     * 退出
     *
     * @param request 请求域
     */
    @Override
    public void logout(HttpServletRequest request) {
        // 删除缓存中的用户信息
        String token = request.getHeader("token");
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + token);
    }
}

