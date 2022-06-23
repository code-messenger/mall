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
//        testService.testLock();
        testService.testRedisson();
        return Result.ok("测试成功！");
    }

    /**
     * 读锁
     *
     * @return 提示信息
     */
    @GetMapping("/readLock")
    public Result<String> testReadLock() {
        String msg = testService.testReadLock();
        return Result.ok(msg);
    }

    /**
     * 写锁
     *
     * @return 提示信息
     */
    @GetMapping("/writeLock")
    public Result<String> testWriteLock() {
        String msg = testService.testWriteLock();
        return Result.ok(msg);
    }

}
