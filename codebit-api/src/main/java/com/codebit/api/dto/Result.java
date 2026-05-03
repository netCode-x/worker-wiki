package com.codebit.api.dto;

import lombok.Data;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description:  统一返回格式化处理
 * @VERSON: 17
 */

@Data
public class Result<T> {


    private Integer code;
    private String msg;
    private T data;


    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 返回成功格式
     * @param data
     * @return
     * @param <T>
     */
    public  static <T> Result<T> success(T data) {
        return new Result<T>(200, "success", data);
    }

    /**
     *  返回失败格式
     * @param code
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<T>(code, msg,null);
    }



}
