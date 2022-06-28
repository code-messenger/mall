package cool.yunlong.mall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yunlong
 * @since 2022/6/28 15:10
 */
@EnableFeignClients(basePackages = "cool.yunlong.mall")
@EnableDiscoveryClient
@ComponentScan(basePackages = "cool.yunlong.mall")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCartApplication.class, args);
    }
}
