package com.demoform.formengine.dto;

import lombok.Data;

/**
 * 更新表单模板请求
 */
@Data
public class TemplateUpdateRequest {
    private String name;
    private String description;
    private String schemaJson;
}
