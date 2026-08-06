package com.shdata.datachain.common.exception;

import java.util.Map;

/**
 * 业务异常，携带 HTTP 状态码、错误码与可选数据，供 Controller 层统一转换为 HTTP 响应。
 *
 * @author ZhangTao
 */
public class BusinessException extends RuntimeException {

    private final int httpStatus;
    private final String code;
    private final String message;
    private final Map<String, Object> data;

    /**
     * @param httpStatus HTTP 状态码
     * @param code       业务错误码
     * @param message    错误描述
     * @param data       附加数据，可为 null
     */
    public BusinessException(int httpStatus, String code, String message, Map<String, Object> data) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Map<String, Object> data() {
        return data;
    }
}
