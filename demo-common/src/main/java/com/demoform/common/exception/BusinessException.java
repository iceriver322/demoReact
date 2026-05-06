package com.demoform.common.exception;

import com.demoform.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常 —— 统一由全局异常处理器捕获并转换为 ApiResponse
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
