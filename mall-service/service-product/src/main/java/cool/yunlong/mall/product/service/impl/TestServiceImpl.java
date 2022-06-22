package cool.yunlong.mall.product.service.impl;

import cool.yunlong.mall.product.service.TestService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 1. 在缓存中设置一个 num 的 key, value 为 0
     * 2. 获取 num 对应的值
     * 如果不为空，则对其进行 + 1，并将结果写入缓存
     * 如果为空，直接返回，停止运行
     */

/*    @Override
    public synchronized void testLock() {
        String num = redisTemplate.opsForValue().get("num");

        // 判断是否为空
        if (StringUtils.isEmpty(num)) {
            // 直接返回
            return;
        }

        // + 1 操作
        int numValue = Integer.parseInt(num);

        // 写入缓存
        redisTemplate.opsForValue().set("num", String.valueOf(++numValue));
    }*/

    /**
     * 用redis做分布式锁
     */
    @Override
    public void testLock() {
        //       try {
        // setnx key value;
//            Boolean result = redisTemplate.opsForValue().setIfAbsent("lock", "yunlong");
        // 使用uuid做为键的值
        String uuid = UUID.randomUUID().toString();
        // set key value ex nx
//            Boolean result = redisTemplate.opsForValue().setIfAbsent("lock", "yunlong", 3, TimeUnit.SECONDS);
        Boolean result = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(result)) {
            // 获取到了锁
            String num = redisTemplate.opsForValue().get("num");

            // 判断是否为空
            if (StringUtils.isEmpty(num)) {
                // 直接返回
                return;
            }

            // + 1 操作
            int numValue = Integer.parseInt(num);


            // 写入缓存
            redisTemplate.opsForValue().set("num", String.valueOf(++numValue));

            //  定义一个lua 脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            //  准备执行lua 脚本
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            //  将lua脚本放入DefaultRedisScript 对象中
            redisScript.setScriptText(script);
            //  设置DefaultRedisScript 这个对象的泛型
            redisScript.setResultType(Long.class);
            //  执行删除
            redisTemplate.execute(redisScript, Collections.singletonList("lock"), uuid);

            // 删除锁  防止其他线程误删锁  自己的锁只能由自己释放
//                if (uuid.equals(redisTemplate.opsForValue().get("lock"))) {
//                    redisTemplate.delete("lock");
//                }
        } else {
            // 没有获取到锁
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            testLock();
        }
//        } catch (RuntimeException e) {
//            throw new RuntimeException(e);
        //       } finally {
        // 解锁
//            redisTemplate.delete("lock");
        //       }
    }
}
