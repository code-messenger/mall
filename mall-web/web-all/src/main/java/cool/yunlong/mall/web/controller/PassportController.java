package cool.yunlong.mall.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yunlong
 * @since 2022/6/27 23:23
 */
@Controller
public class PassportController {

    @GetMapping("login.html")
    public String login(HttpServletRequest request) {
        // 获取请求路径
        String originUrl = request.getParameter("originUrl");
        // 存到 request 域中
        request.setAttribute("originUrl", originUrl);

        return "login";
    }
}
