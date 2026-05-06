package com.demoform.web.controller;

import com.demoform.common.dto.*;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.entity.SysUser;
import com.demoform.user.service.UserService;
import com.demoform.web.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 认证控制器 —— 注册、登录、获取当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /** 用户注册 */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success();
    }

    /** 用户登录 —— 验证用户名密码，返回 JWT Token */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Spring Security 认证
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        // 查询用户信息
        SysUser user = userService.findByUsername(request.getUsername());
        // 检查账户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        // 生成 JWT Token
        List<String> roles = userService.getUserRoleCodes(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
        // 构建响应
        LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), roles);
        return ApiResponse.success(resp);
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public ApiResponse<?> me() {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(userService.getUserDetail(userId));
    }
}
