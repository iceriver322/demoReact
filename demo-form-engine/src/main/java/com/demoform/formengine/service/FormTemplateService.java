package com.demoform.formengine.service;

import com.demoform.common.dto.PageResult;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.dto.TemplateUpdateRequest;
import com.demoform.formengine.entity.FormTemplate;

/**
 * 表单模板服务接口
 */
public interface FormTemplateService {

    FormTemplate create(Long ownerId, TemplateCreateRequest request);

    FormTemplate update(Long templateId, Long userId, TemplateUpdateRequest request);

    void delete(Long templateId, Long userId);

    FormTemplate findById(Long templateId);

    PageResult<FormTemplate> listMyTemplates(int page, int size, Long ownerId);

    PageResult<FormTemplate> listPublishedTemplates(int page, int size);

    void publish(Long templateId, Long userId);

    void disable(Long templateId, Long userId);
}
