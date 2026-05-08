package com.demoform.user.service;

import com.demoform.common.dto.PageResult;
import com.demoform.common.dto.RegisterRequest;
import com.demoform.user.dto.*;
import com.demoform.user.entity.SysUser;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    SysUser findByUsername(String username);

    SysUser findById(Long userId);

    void register(RegisterRequest request);

    PageResult<UserVO> listUsers(int page, int size, String username);

    UserVO getUserDetail(Long userId);

    void updateUser(Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    void assignRoles(Long userId, RoleAssignRequest request);

    List<String> getUserRoleCodes(Long userId);

    /** 记录登录失败，检查是否需要锁定 */
    void recordLoginFailure(String username);

    /** 重置登录失败计数（登录成功时调用） */
    void resetLoginAttempts(String username);

    /** 检查账户是否被锁定 */
    void checkAccountLocked(String username);

    /** 管理员解锁用户 */
    void unlockUser(Long userId);
}
