package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.dto.PageResult;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.dto.TemplateUpdateRequest;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.service.FormTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 表单模板控制器 —— 创建、编辑、删除、发布、停用表单
 */
@RestController
@RequestMapping("/api/forms/templates")
@RequiredArgsConstructor
public class FormTemplateController {

    private final FormTemplateService templateService;

    /** 创建表单模板 */
    @PostMapping
    public ApiResponse<FormTemplate> create(@Valid @RequestBody TemplateCreateRequest request,
                                              Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(templateService.create(userId, request));
    }

    /** 我的表单列表 */
    @GetMapping
    public ApiResponse<PageResult<FormTemplate>> listMine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(templateService.listMyTemplates(page, size, userId));
    }

    /** 已发布的表单列表（供用户填报入口） */
    @GetMapping("/published")
    public ApiResponse<PageResult<FormTemplate>> listPublished(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(templateService.listPublishedTemplates(page, size));
    }

    /** 表单模板详情 */
    @GetMapping("/{id}")
    public ApiResponse<FormTemplate> detail(@PathVariable Long id) {
        return ApiResponse.success(templateService.findById(id));
    }

    /** 编辑表单模板 */
    @PutMapping("/{id}")
    public ApiResponse<FormTemplate> update(@PathVariable Long id,
                                              @Valid @RequestBody TemplateUpdateRequest request,
                                              Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(templateService.update(id, userId, request));
    }

    /** 删除表单模板 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        templateService.delete(id, userId);
        return ApiResponse.success();
    }

    /** 发布表单 */
    @PutMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        templateService.publish(id, userId);
        return ApiResponse.success();
    }

    /** 停用表单 */
    @PutMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        templateService.disable(id, userId);
        return ApiResponse.success();
    }
}
