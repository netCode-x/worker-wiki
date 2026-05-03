package com.codebit.api.utils;

import lombok.Getter;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description: 自定义业务异常
 * @VERSON: 17
 */

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;


    public BusinessException(final Integer code, final String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message){
        this(500, message);
    }
}
