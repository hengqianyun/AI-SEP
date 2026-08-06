package com.shdata.datachain.common.response;

import com.shdata.datachain.common.support.ApiEnvelope;
import java.util.Map;
import org.springframework.http.ResponseEntity;

/**
 * 统一 Service 层结果封装，携带 ok/httpStatus/code/message/data，
 * 并提供 {@link #toResponseEntity(String)} 直接转换为 HTTP 响应，消除 Controller 层重复代码。
 *
 * @author ZhangTao
 */
public record ServiceResult(boolean ok, int httpStatus, String code, String message, Object data) {

    /** 成功结果（HTTP 200）。 */
    public static ServiceResult ok(Object data) {
        return new ServiceResult(true, 200, "0", "ok", data);
    }

    /** 失败结果。 */
    public static ServiceResult fail(int httpStatus, String code, String message, Object data) {
        return new ServiceResult(false, httpStatus, code, message, data);
    }

    /**
     * 将自身转换为 {@link ResponseEntity}，成功用 {@link ApiEnvelope#ok} 包裹，失败用 {@link ApiEnvelope#error}。
     *
     * @param correlationId 请求追踪 ID
     * @return HTTP 响应体
     */
    public ResponseEntity<Map<String, Object>> toResponseEntity(String correlationId) {
        if (ok) {
            return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
        }
        return ResponseEntity.status(httpStatus)
                .body(ApiEnvelope.error(code, message, data, correlationId));
    }
}
