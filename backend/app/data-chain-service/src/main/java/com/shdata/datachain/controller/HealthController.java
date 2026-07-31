package com.shdata.datachain.controller;

import com.shdata.datachain.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "健康检查")
@RestController
public class HealthController {

    @Operation(summary = "检查服务状态")
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("UP");
    }
}

