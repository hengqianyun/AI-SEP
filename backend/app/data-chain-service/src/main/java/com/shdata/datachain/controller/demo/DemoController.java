package com.shdata.datachain.controller.demo;

import com.shdata.datachain.common.response.ApiResponse;
import com.shdata.datachain.entity.DemoEntity;
import com.shdata.datachain.model.DemoRequest;
import com.shdata.datachain.service.demo.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demos")
@Tag(name = "Demo 管理", description = "基础 CRUD 与 QueryDSL 分页查询示例")
public class DemoController {

    private final DemoService demoService;

    @PostMapping
    @Operation(summary = "新增 Demo")
    public ApiResponse<DemoEntity> create(@Valid @RequestBody DemoRequest request) {
        return ApiResponse.success(demoService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询 Demo 详情")
    public ApiResponse<DemoEntity> get(
            @Parameter(description = "Demo ID")
            @PathVariable Long id) {
        return ApiResponse.success(demoService.get(id));
    }

    @GetMapping
    @Operation(summary = "分页查询 Demo")
    public ApiResponse<Page<DemoEntity>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.success(demoService.page(keyword, page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新 Demo")
    public ApiResponse<DemoEntity> update(
            @PathVariable Long id,
            @Valid @RequestBody DemoRequest request) {
        return ApiResponse.success(demoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除 Demo")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        demoService.delete(id);
        return ApiResponse.success();
    }
}

