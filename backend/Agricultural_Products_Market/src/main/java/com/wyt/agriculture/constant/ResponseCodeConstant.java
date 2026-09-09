package com.wyt.agriculture.constant;

public class ResponseCodeConstant {

    // 成功响应码
    public static final Integer SUCCESS = 200;

    // 客户端错误响应码
    public static final Integer BAD_REQUEST = 400;
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer FORBIDDEN = 403;
    public static final Integer NOT_FOUND = 404;
    public static final Integer METHOD_NOT_ALLOWED = 405;
    public static final Integer CONFLICT = 409;
    public static final Integer UNPROCESSABLE_ENTITY = 422;

    // 服务器错误响应码
    public static final Integer INTERNAL_SERVER_ERROR = 500;
    public static final Integer SERVICE_UNAVAILABLE = 503;
}
