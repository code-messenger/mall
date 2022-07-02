package cool.yunlong.mall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yunlong
 * @since 2022/7/1 19:59
 */
@SpringBootApplication
@ComponentScan({"cool.yunlong.mall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"cool.yunlong.mall"})
public class ServicePaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicePaymentApplication.class, args);
    }
}