package com.demoform.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员编辑用户请求
 */
@Data
public class UserUpdateRequest {
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;
    private String email;
    private Integer status;
}
