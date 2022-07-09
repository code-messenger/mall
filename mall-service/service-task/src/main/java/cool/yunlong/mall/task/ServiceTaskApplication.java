package cool.yunlong.mall.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yunlong
 * @since 2022/7/2 18:35
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan({"cool.yunlong.mall"})
@EnableDiscoveryClient
public class ServiceTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceTaskApplication.class, args);
    }
}
