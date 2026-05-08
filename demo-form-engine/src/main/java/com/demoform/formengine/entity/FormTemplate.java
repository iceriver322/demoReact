package com.demoform.formengine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demoform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表单模板实体 —— schema_json 存储拖拽设计器生成的字段定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_template")
public class FormTemplate extends BaseEntity {

    /** 表单名称 */
    private String name;

    /** 表单描述 */
    private String description;

    /** 创建者ID */
    private Long ownerId;

    /** 表单字段定义 JSON（schema_json 映射为 schemaJson） */
    @TableField("schema_json")
    private String schemaJson;

    /** 状态：DRAFT / PUBLISHED / DISABLED */
    private String status;

    /** 是否需要审批（默认 true） */
    private Boolean needApproval;
}
