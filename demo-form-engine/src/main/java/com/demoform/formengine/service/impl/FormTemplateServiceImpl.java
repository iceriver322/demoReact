package com.demoform.formengine.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.common.dto.PageResult;
import com.demoform.common.enums.FormStatus;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.dto.TemplateUpdateRequest;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.formengine.service.FormTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 表单模板服务实现
 */
@Service
@RequiredArgsConstructor
public class FormTemplateServiceImpl implements FormTemplateService {

    private final FormTemplateMapper formTemplateMapper;

    @Override
    @Transactional
    public FormTemplate create(Long ownerId, TemplateCreateRequest request) {
        FormTemplate template = new FormTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setOwnerId(ownerId);
        template.setSchemaJson(request.getSchemaJson());
        template.setStatus(FormStatus.DRAFT.name());
        formTemplateMapper.insert(template);
        return template;
    }

    @Override
    @Transactional
    public FormTemplate update(Long templateId, Long userId, TemplateUpdateRequest request) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        if (request.getName() != null) template.setName(request.getName());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getSchemaJson() != null) template.setSchemaJson(request.getSchemaJson());
        formTemplateMapper.updateById(template);
        return template;
    }

    @Override
    @Transactional
    public void delete(Long templateId, Long userId) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        formTemplateMapper.deleteById(templateId);
    }

    @Override
    public FormTemplate findById(Long templateId) {
        FormTemplate template = formTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        return template;
    }

    @Override
    public PageResult<FormTemplate> listMyTemplates(int page, int size, Long ownerId) {
        Page<FormTemplate> pageParam = new Page<>(page, size);
        IPage<FormTemplate> result = formTemplateMapper.selectMyTemplates(pageParam, ownerId);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public PageResult<FormTemplate> listPublishedTemplates(int page, int size) {
        Page<FormTemplate> pageParam = new Page<>(page, size);
        IPage<FormTemplate> result = formTemplateMapper.selectPublishedTemplates(pageParam);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    @Transactional
    public void publish(Long templateId, Long userId) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        if (FormStatus.PUBLISHED.name().equals(template.getStatus())) {
            throw new BusinessException(ResultCode.FORM_ALREADY_PUBLISHED);
        }
        template.setStatus(FormStatus.PUBLISHED.name());
        formTemplateMapper.updateById(template);
    }

    @Override
    @Transactional
    public void disable(Long templateId, Long userId) {
        FormTemplate template = findById(templateId);
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        template.setStatus(FormStatus.DISABLED.name());
        formTemplateMapper.updateById(template);
    }
}
