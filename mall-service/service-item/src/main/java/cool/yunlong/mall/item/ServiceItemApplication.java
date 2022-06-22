package cool.yunlong.mall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 远程调用其他微服务接口进行数据汇总，封装商品详情页面所需数据给 web-all 渲染页面
 *
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
