package com.demoform.common.enums;

import lombok.Getter;

/**
 * 统一返回码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_FAILED(422, "参数校验失败"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 - 用户
    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_WRONG(1003, "密码错误"),
    PASSWORD_EXPIRED(1004, "密码已过期，请修改密码"),
    ACCOUNT_LOCKED(1005, "账户已被锁定，请30分钟后重试"),
    ACCOUNT_DISABLED(1006, "账户已被禁用"),
    PASSWORD_WEAK(1007, "密码强度不足，至少8位含大小写字母和数字"),
    ROLE_NOT_FOUND(1008, "角色不存在"),

    // 业务错误码 - 表单
    FORM_NOT_FOUND(2001, "表单不存在"),
    FORM_ALREADY_PUBLISHED(2002, "表单已发布"),
    FORM_NOT_PUBLISHED(2003, "表单未发布"),
    SUBMISSION_NOT_FOUND(2004, "填报数据不存在"),
    ALREADY_APPROVED(2005, "数据已审批"),
    NOT_FORM_OWNER(2006, "非表单所有者"),

    // 业务错误码 - 审批
    APPROVAL_TASK_NOT_FOUND(3001, "审批任务不存在"),
    /** 不能审批自己的提交 */
    CANNOT_APPROVE_SELF(3002, "不能审批自己的提交");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
