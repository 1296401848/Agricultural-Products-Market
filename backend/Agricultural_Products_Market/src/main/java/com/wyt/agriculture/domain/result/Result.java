package com.wyt.agriculture.domain.result;

import com.wyt.agriculture.constant.ResponseCodeConstant;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：200成功，400失败
    private String msg; //返回信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 200;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 200;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = ResponseCodeConstant.BAD_REQUEST;
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = code;
        return result;
    }

    public static <T> Result<T> unauthorized(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = ResponseCodeConstant.UNAUTHORIZED;
        return result;
    }

    public static <T> Result<T> forbidden(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = ResponseCodeConstant.FORBIDDEN;
        return result;
    }

    public static <T> Result<T> notFound(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = ResponseCodeConstant.NOT_FOUND;
        return result;
    }

    public static <T> Result<T> success(T object, String msg) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.msg = msg;
        result.code = ResponseCodeConstant.SUCCESS;
        return result;
    }

}
