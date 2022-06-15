package cool.yunlong.mall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yunlong
 * @since 2022/6/14 17:22
 */
@EnableFeignClients(basePackages = "cool.yunlong.mall")
@EnableDiscoveryClient
@ComponentScan(basePackages = "cool.yunlong.mall")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceItemApplication.class, args);
    }
}
