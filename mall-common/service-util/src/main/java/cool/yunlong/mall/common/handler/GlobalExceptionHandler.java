package cool.yunlong.mall.common.handler;

import cool.yunlong.mall.common.execption.MallException;
import cool.yunlong.mall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 *
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    /**
     * 自定义异常处理方法
     *
     * @param e 异常对象
     * @return Result对象
     */
    @ExceptionHandler(MallException.class)
    @ResponseBody
    public Result error(MallException e) {
        return Result.fail(e.getMessage());
    }
}
