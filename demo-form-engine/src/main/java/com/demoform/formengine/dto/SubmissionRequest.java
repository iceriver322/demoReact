package com.demoform.formengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提报表单数据请求
 */
@Data
public class SubmissionRequest {
    @NotNull(message = "表单模板ID不能为空")
    private Long templateId;

    @NotBlank(message = "填报数据不能为空")
    private String dataJson;
}
