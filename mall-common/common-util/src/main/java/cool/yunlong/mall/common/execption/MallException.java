package cool.yunlong.mall.common.execption;

import cool.yunlong.mall.common.result.ResultCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义全局异常类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "自定义全局异常类")
public class MallException extends RuntimeException {

    @ApiModelProperty(value = "异常状态码")
    private Integer code;

    /**
     * 通过状态码和错误消息创建异常对象
     *
     * @param message 错误消息
     * @param code    状态码
     */
    public MallException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    /**
     * 通过状态码和错误消息创建异常对象
     *
     * @param resultCodeEnum 状态码枚举
     */
    public MallException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "MallException{" + "code=" + code + ", message=" + this.getMessage() + '}';
    }
}
