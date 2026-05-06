package com.demoform.formengine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demoform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 表单填报数据实体 —— data_json 存储用户提交的表单数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_submission")
public class FormSubmission extends BaseEntity {

    /** 表单模板ID */
    private Long templateId;

    /** 提交者ID */
    private Long submitterId;

    /** 填报数据 JSON */
    @TableField("data_json")
    private String dataJson;

    /** 状态：PENDING / APPROVED / REJECTED */
    private String status;

    /** 审批人ID */
    private Long approverId;

    /** 审批时间 */
    private LocalDateTime approvedAt;
}
