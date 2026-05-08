package com.demoform.web.controller;

import com.demoform.common.dto.*;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.dto.ChangePasswordRequest;
import com.demoform.user.entity.SysUser;
import com.demoform.user.service.UserService;
import com.demoform.web.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
        // 先检查账户是否被锁定
        userService.checkAccountLocked(request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            // 记录登录失败
            userService.recordLoginFailure(request.getUsername());
            throw new BusinessException(ResultCode.PASSWORD_WRONG);
        }

        SysUser user = userService.findByUsername(request.getUsername());

        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 检查密码是否过期
        if (user.getPasswordExpireDate() != null &&
                user.getPasswordExpireDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ResultCode.PASSWORD_EXPIRED);
        }

        // 登录成功，重置失败计数
        userService.resetLoginAttempts(request.getUsername());

        List<String> roles = userService.getUserRoleCodes(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
        LoginResponse resp = new LoginResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), roles);
        return ApiResponse.success(resp);
    }

    /** 修改密码（密码过期时使用，请求体中包含 username + oldPassword + newPassword） */
    @PutMapping("/password")
    public ApiResponse<LoginResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        SysUser user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 调用 UserService 验证旧密码并更新
        userService.changePassword(user.getId(), request);

        userService.resetLoginAttempts(request.getUsername());

        List<String> roles = userService.getUserRoleCodes(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);
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
