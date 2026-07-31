package com.shdata.datachain.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@Schema(description = "统一接口响应")
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "业务状态码", example = "200")
    private final int code;

    @Schema(description = "响应信息", example = "success")
    private final String message;

    @Schema(description = "响应数据")
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

