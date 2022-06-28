package cool.yunlong.mall.user.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.model.user.UserInfo;
import cool.yunlong.mall.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yunlong
 * @since 2022/6/27 21:41
 */
@Api(tags = "用户认证接口")
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody UserInfo loginUser, HttpServletRequest httpServletRequest) {
        // 登录成功封装的结果
        Map map = userService.login(loginUser, httpServletRequest);
        if (map == null) {
            return Result.fail().message("登录失败");
        } else {
            return Result.ok(map);
        }
    }

    @Operation(summary = "退出登录")
    @GetMapping("logout")
    public Result<Void> logout(HttpServletRequest request) {

        // 用户退出
        userService.logout(request);

        return Result.ok();
    }
}
