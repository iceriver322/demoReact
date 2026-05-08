package com.demoform.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.common.dto.PageResult;
import com.demoform.common.dto.RegisterRequest;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.dto.*;
import com.demoform.user.entity.SysUser;
import com.demoform.user.entity.UserRole;
import com.demoform.user.mapper.UserMapper;
import com.demoform.user.mapper.UserRoleMapper;
import com.demoform.user.service.RoleService;
import com.demoform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public SysUser findById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        SysUser existing = userMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        // 校验密码强度：至少8位，含大小写字母和数字
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new BusinessException(ResultCode.PASSWORD_WEAK);
        }
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setPasswordExpireDate(LocalDate.now().plusDays(90));
        userMapper.insert(user);
        // 分配普通用户角色
        UserRole ur = new UserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(roleService.findByCode("ROLE_USER").getId());
        userRoleMapper.insert(ur);
    }

    @Override
    public PageResult<UserVO> listUsers(int page, int size, String username) {
        Page<UserVO> pageParam = new Page<>(page, size);
        IPage<UserVO> result = userMapper.selectUserPage(pageParam, username);
        // 填充角色列表
        for (UserVO vo : result.getRecords()) {
            vo.setRoles(userMapper.selectRoleCodesByUserId(vo.getId()));
        }
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        UserVO vo = userMapper.selectUserDetail(userId);
        if (vo == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        vo.setRoles(userMapper.selectRoleCodesByUserId(userId));
        return vo;
    }

    @Override
    @Transactional
    public void updateUser(Long userId, UserUpdateRequest request) {
        SysUser user = findById(userId);
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        findById(userId);
        userMapper.deleteById(userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = findById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_WRONG);
        }
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new BusinessException(ResultCode.PASSWORD_WEAK);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpireDate(LocalDate.now().plusDays(90));
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, RoleAssignRequest request) {
        findById(userId);
        userRoleMapper.deleteByUserId(userId);
        userRoleMapper.insertBatch(userId, request.getRoleIds());
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    @Transactional
    public void recordLoginFailure(String username) {
        SysUser user = findByUsername(username);
        if (user == null) return;

        int attempts = (user.getLoginAttempts() == null ? 0 : user.getLoginAttempts()) + 1;
        user.setLoginAttempts(attempts);

        if (attempts >= 3) {
            user.setLockTime(LocalDateTime.now());
        }

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void resetLoginAttempts(String username) {
        SysUser user = findByUsername(username);
        if (user == null) return;
        user.setLoginAttempts(0);
        user.setLockTime(null);
        userMapper.updateById(user);
    }

    @Override
    public void checkAccountLocked(String username) {
        SysUser user = findByUsername(username);
        if (user == null) return;

        LocalDateTime lockTime = user.getLockTime();
        if (lockTime != null) {
            // 检查是否已过30分钟
            if (lockTime.plusMinutes(30).isAfter(LocalDateTime.now())) {
                throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
            } else {
                // 已过30分钟，自动解锁
                user.setLoginAttempts(0);
                user.setLockTime(null);
                userMapper.updateById(user);
            }
        }
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setLoginAttempts(0);
        user.setLockTime(null);
        userMapper.updateById(user);
    }
}
