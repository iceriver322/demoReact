package com.demoform.web.config;

import com.demoform.user.entity.SysUser;
import com.demoform.user.entity.UserRole;
import com.demoform.user.mapper.UserMapper;
import com.demoform.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 数据初始化器 —— 确保 admin 管理员用户存在，密码正确加密
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        SysUser existing = userMapper.selectByUsername("admin");
        if (existing != null) {
            // 确保 admin 密码正确加密（防止 SQL 中占位哈希无效）
            existing.setPassword(passwordEncoder.encode("admin"));
            existing.setPasswordExpireDate(LocalDate.now().minusDays(1)); // 首次登录强制改密
            userMapper.updateById(existing);
            log.info("管理员 admin 已更新");
        } else {
            log.warn("管理员 admin 不存在，请检查初始数据");
        }
    }
}
