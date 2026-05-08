package com.demoform.workflow.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * Camunda 用户任务 DTO
 */
@Data
@Builder
public class TaskDto {
    private String taskId;
    private String processInstanceId;
    private String name;
    private Date createTime;
    private Map<String, Object> variables;
    /** 填报数据 JSON */
    private String submissionData;
    /** 表单模板名称 */
    private String templateName;
    /** 表单模板 schema JSON */
    private String schemaJson;
}
