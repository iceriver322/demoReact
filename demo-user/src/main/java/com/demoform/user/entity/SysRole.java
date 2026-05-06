package com.demoform.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统角色实体
 */
@Data
@TableName("sys_role")
public class SysRole {

    private Long id;
    /** 角色名称 */
    private String name;
    /** 角色编码：ROLE_ADMIN / ROLE_PRIVILEGED / ROLE_USER */
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
