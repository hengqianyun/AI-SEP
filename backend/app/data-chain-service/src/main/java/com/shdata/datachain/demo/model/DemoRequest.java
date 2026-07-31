package com.shdata.datachain.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Schema(description = "Demo 新增或更新请求")
public class DemoRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过 100 个字符")
    @Schema(description = "名称", required = true, example = "Demo 名称")
    private String name;

    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    @Schema(description = "描述", example = "Demo 描述")
    private String description;
}

