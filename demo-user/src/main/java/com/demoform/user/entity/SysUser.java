package com.demoform.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.demoform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/**
 * 系统用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 用户名 */
    private String username;

    /** 密码（bcrypt 加密存储） */
    private String password;

    /** 邮箱 */
    private String email;

    /** 状态：1-正常 0-禁用 */
    private Integer status;

    /** 密码过期日期 */
    private LocalDate passwordExpireDate;
}
