package com.demoform.formengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提报表单数据请求
 */
@Data
public class SubmissionRequest {
    @NotBlank(message = "填报数据不能为空")
    private String dataJson;
}
