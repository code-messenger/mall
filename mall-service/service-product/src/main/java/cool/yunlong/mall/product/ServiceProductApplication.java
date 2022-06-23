package cool.yunlong.mall.product;

import cool.yunlong.mall.common.constant.RedisConst;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"cool.yunlong.mall"})
@EnableDiscoveryClient
public class ServiceProductApplication implements CommandLineRunner {

    @Autowired
    private RedissonClient redissonClient;

    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 初始化布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);

        // 设置误判率    第一个参数：数据规模 第二个参数：误判率    自动计算有多少个哈希函数，以及二进制数组长度
        bloomFilter.tryInit(100000, 0.001);
    }
}
