package com.demoform.formengine.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.common.dto.PageResult;
import com.demoform.common.enums.FormStatus;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.enums.SubmissionStatus;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.formengine.service.FormSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 表单填报服务实现
 */
@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private final FormSubmissionMapper submissionMapper;
    private final FormTemplateMapper templateMapper;

    @Override
    @Transactional
    public FormSubmission submit(Long templateId, Long submitterId, String dataJson) {
        // 校验表单模板存在且已发布
        FormTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        if (!FormStatus.PUBLISHED.name().equals(template.getStatus())) {
            throw new BusinessException(ResultCode.FORM_NOT_PUBLISHED);
        }
        // 创建填报记录，状态为待审批
        FormSubmission submission = new FormSubmission();
        submission.setTemplateId(templateId);
        submission.setSubmitterId(submitterId);
        submission.setDataJson(dataJson);
        submission.setStatus(SubmissionStatus.PENDING.name());
        submissionMapper.insert(submission);
        return submission;
    }

    @Override
    public PageResult<FormSubmission> listByTemplate(int page, int size,
                                                      Long templateId, Long userId) {
        // 校验表单所有权
        FormTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        Page<FormSubmission> pageParam = new Page<>(page, size);
        IPage<FormSubmission> result = submissionMapper.selectByTemplateId(pageParam, templateId);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public PageResult<FormSubmission> listMySubmissions(int page, int size, Long submitterId) {
        Page<FormSubmission> pageParam = new Page<>(page, size);
        IPage<FormSubmission> result = submissionMapper.selectMySubmissions(pageParam, submitterId);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }

    @Override
    public List<FormSubmission> listMyByTemplateId(Long templateId, Long submitterId) {
        return submissionMapper.selectMyByTemplateId(templateId, submitterId);
    }

    @Override
    public List<FormSubmission> exportByTemplate(Long templateId, Long userId) {
        FormTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FORM_OWNER);
        }
        return submissionMapper.selectAllByTemplateId(templateId);
    }

    @Override
    public FormSubmission findById(Long submissionId) {
        FormSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCode.SUBMISSION_NOT_FOUND);
        }
        return submission;
    }
}
