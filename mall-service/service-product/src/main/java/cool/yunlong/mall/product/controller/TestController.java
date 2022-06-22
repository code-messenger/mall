package cool.yunlong.mall.product.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.product.service.TestService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping("admin/product/test")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/testLock")
    public Result<String> testLock() {
        testService.testLock();
        return Result.ok("测试成功！");
    }
}
