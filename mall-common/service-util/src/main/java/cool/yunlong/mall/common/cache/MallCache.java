package cool.yunlong.mall.common.cache;

import java.lang.annotation.*;

/**
 * @author atguigu-mqx
 */
@Target({ElementType.METHOD})   // 当前注解的使用范围 在方法上使用！
@Retention(RetentionPolicy.RUNTIME) // 当前注解的生命周期 当前这个注解在 .java .class 在Java虚拟机上
@Inherited
@Documented
public @interface MallCache {

    //  定义一个数据 sku:skuId
    //  目的用这个前缀要想组成 缓存的key！
    String prefix() default "cache:";

    // 表示后缀
    String suffix() default ":info";

}
