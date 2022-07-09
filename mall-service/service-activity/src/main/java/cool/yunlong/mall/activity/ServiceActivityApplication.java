package cool.yunlong.mall.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yunlong
 * @since 2022/7/2 18:30
 */
@EnableFeignClients(basePackages = "cool.yunlong.mall")
@ComponentScan(basePackages = "cool.yunlong.mall")
@EnableDiscoveryClient
@SpringBootApplication
public class ServiceActivityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceActivityApplication.class, args);
    }
}
