package cool.yunlong.mall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yunlong
 * @since 2022/6/28 22:17
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "cool.yunlong.mall")
@ComponentScan(basePackages = "cool.yunlong.mall")
@SpringBootApplication
public class ServiceOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}
