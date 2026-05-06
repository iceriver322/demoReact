package com.demoform.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 表单数据平台 - 应用启动入口
 */
@SpringBootApplication(scanBasePackages = "com.demoform")
public class FormApplication {
    public static void main(String[] args) {
        SpringApplication.run(FormApplication.class, args);
    }
}
