package cool.yunlong.mall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.common.result.ResultCodeEnum;
import cool.yunlong.mall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 全局认证过滤器
 *
 * @author yunlong
 * @since 2022/6/28 10:04
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    // 创建一个匹配格式对象
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    private String authUrl;

    @Autowired
    private RedisTemplate redisTemplate;

    /***
     * 全局过滤器
     * @param exchange  网络服务对象
     * @param chain 过滤器链
     * @return Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取当前请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 获取当前请求的 url
        String path = request.getURI().getPath();

        // 获取当前响应对象
        ServerHttpResponse response = exchange.getResponse();

        // 判断是否属于内部数据接口
        if (antPathMatcher.match("/**/inner/**", path)) {
            return out(response, ResultCodeEnum.PERMISSION);
        }

        // 判断用户访问的请求中是否携带 /auth/ ,如果携带了必须要经过登录才能放行，没有登录要给出提示信息
        // 获取缓存中的用户 id
        String userId = getUserId(request);
        // 校验IP地址
        if ("-1".equals(userId)) {
            return out(response, ResultCodeEnum.PERMISSION);
        }

        if (antPathMatcher.match("/api/**/auth/**", path)) {
            // 判断当前是否登录
            if (StringUtils.isEmpty(userId)) {
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        // 判断是否包含需要登录的页面  trade.html, myOrder.html
        String[] split = authUrl.split(",");
        if (split.length > 0) {
            for (String url : split) {
                // 判断 path 中是否包含url
                if (path.contains(url) && StringUtils.isEmpty(userId)) {
                    // 重定向
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.mall.com/login.html?originUrl=" + request.getURI());

                    return response.setComplete();
                }
            }
        }
        if (!StringUtils.isEmpty(userId)) {
            // 将用户id 放入请求头
            request.mutate().header("userId", userId).build();
            ServerWebExchange build = exchange.mutate().request(request).build();
            return chain.filter(build);
        }
        // 默认返回
        return chain.filter(exchange);
    }

    /**
     * 用户输出提示方法
     *
     * @param response       响应对象
     * @param resultCodeEnum 响应状态值
     * @return Mono
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result<Object> result = Result.build(null, resultCodeEnum);
        String jsonString = JSON.toJSONString(result);
        DataBuffer wrap = response.bufferFactory().wrap(jsonString.getBytes());
        // 设置请求头
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        // 响应数据
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * 获取缓存中的 userId
     *
     * @param request 请求域对象
     * @return userId
     */
    private String getUserId(ServerHttpRequest request) {
        // 用户 token
        String token = "";
        // 从 headers 或 cookie 中获取 token
        List<String> stringList = request.getHeaders().get("token");
        // 判空
        if (!CollectionUtils.isEmpty(stringList)) {
            token = stringList.get(0);
        } else {
            HttpCookie token1 = request.getCookies().getFirst("token");
            if (token1 != null) {
                token = token1.getValue();
            }
        }

        if (!StringUtils.isEmpty(token)) {
            // 缓存key
            String loginKey = "user:login:" + token;
            // 获取数据
            String userIdString = (String) redisTemplate.opsForValue().get(loginKey);
            // 转换为json
            JSONObject jsonObject = JSONObject.parseObject(userIdString);
            if (jsonObject != null) {

                String ip = (String) jsonObject.get("ip");
                // 校验 ip
                if (ip.equals(IpUtil.getGatwayIpAddress(request))) {
                    return (String) jsonObject.get("userId");
                } else {
                    return "-1";
                }
            }
        }
        return null;
    }
}

