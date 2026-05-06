package com.demoform.user.service;

import com.demoform.common.dto.RegisterRequest;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.user.entity.SysRole;
import com.demoform.user.entity.SysUser;
import com.demoform.user.mapper.UserMapper;
import com.demoform.user.mapper.UserRoleMapper;
import com.demoform.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private RoleService roleService;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("Abc12345");
        request.setEmail("test@test.com");
    }

    @Test
    void shouldRegisterSuccessfully() {
        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        SysRole role = new SysRole();
        role.setId(3L);
        role.setCode("ROLE_USER");
        when(roleService.findByCode("ROLE_USER")).thenReturn(role);
        when(userMapper.insert(any())).thenReturn(1);
        when(userRoleMapper.insert(any())).thenReturn(1);

        userService.register(request);

        verify(userMapper).insert(any(SysUser.class));
        verify(userRoleMapper).insert(any());
    }

    @Test
    void shouldFailWhenUsernameExists() {
        when(userMapper.selectByUsername("testuser")).thenReturn(new SysUser());

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.USERNAME_EXISTS.getCode());
    }

    @Test
    void shouldFailWhenPasswordWeak() {
        request.setPassword("12345678"); // 不含大小写字母组合
        when(userMapper.selectByUsername("testuser")).thenReturn(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.PASSWORD_WEAK.getCode());
    }

    @Test
    void shouldFailWhenPasswordMissingUpperCase() {
        request.setPassword("abc12345"); // 不含大写字母
        when(userMapper.selectByUsername("testuser")).thenReturn(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.PASSWORD_WEAK.getCode());
    }

    @Test
    void shouldFailWhenUserNotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResultCode.USER_NOT_FOUND.getCode());
    }

    @Test
    void shouldFailWhenDeleteNonexistentUser() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(BusinessException.class);
    }
}
