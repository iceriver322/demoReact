package com.demoform.formengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建表单模板请求
 */
@Data
public class TemplateCreateRequest {
    @NotBlank(message = "表单名称不能为空")
    private String name;
    private String description;
    private String schemaJson;
    private Boolean needApproval;
}
