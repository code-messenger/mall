package cool.yunlong.mall.product.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.product.service.TestService;
import io.swagger.annotations.Api;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    @SneakyThrows
    public static void main(String[] args) {
//        // 无返回值
//        CompletableFuture<Void> hello = CompletableFuture.runAsync(() -> System.out.println("hello"));
//
//        hello.get();
//
//        // 有返回值
//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> 1024);
//        // 获取异步任务返回值
//        integerCompletableFuture.get();

//        // 有返回值
//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> 1024)
//                .whenComplete((i, t) -> {
//                    System.out.println("i:\t" + i);
//                    System.out.println("t:\t" + t);
//                });
//        // 获取异步任务返回值
//        integerCompletableFuture.get();
//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> 1024)
//                .thenApply(i -> i * 2);

//        // 无返回值
//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> 1024)
//                        .thenAccept(new Consumer<Integer>() {
//                            @Override
//                            public void accept(Integer integer) {
//
//                            }
//                        })

//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> 1024)
//                .thenApply(integer -> integer * 2)
//                .whenComplete((i, t) -> {   // 获取执行的返回结果以及可能抛出的异常
//                    System.out.println("i = " + i);
//                    System.out.println("t = " + t);
//                    int i1 = 1 / 0;
//                }).exceptionally(r -> { // 获取执行上一步时可能抛出的异常
//                    System.out.println("r = " + r);
//                    return 200;
//                });
//        System.out.println(integerCompletableFuture.get());

        // 创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                7, 10, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(3)
        );

        CompletableFuture<String> completableFutureA = CompletableFuture.supplyAsync(() -> "hello", threadPoolExecutor);

        CompletableFuture<Void> completableFutureB = completableFutureA.thenAcceptAsync(s -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(s + " B");
        });

        CompletableFuture<Void> completableFutureC = completableFutureA.thenAcceptAsync(s -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(s + " C");
        });

        System.out.println(completableFutureB.get());
        System.out.println(completableFutureC.get());
    }


}
