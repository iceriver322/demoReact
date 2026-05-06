package com.demoform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 —— 返回 JWT Token 和用户基本信息
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private java.util.List<String> roles;
}
