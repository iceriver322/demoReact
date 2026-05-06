package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.dto.PageResult;
import com.demoform.user.dto.*;
import com.demoform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器 —— 管理员操作用户 CRUD、角色分配
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 用户列表（管理员） */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username) {
        return ApiResponse.success(userService.listUsers(page, size, username));
    }

    /** 用户详情 */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserVO> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserDetail(id));
    }

    /** 编辑用户 */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(@PathVariable Long id,
                                     @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success();
    }

    /** 删除用户（逻辑删除） */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    /** 分配角色 */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> assignRoles(@PathVariable Long id,
                                          @Valid @RequestBody RoleAssignRequest request) {
        userService.assignRoles(id, request);
        return ApiResponse.success();
    }

    /** 修改当前用户密码 */
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                             Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userService.changePassword(userId, request);
        return ApiResponse.success();
    }
}
