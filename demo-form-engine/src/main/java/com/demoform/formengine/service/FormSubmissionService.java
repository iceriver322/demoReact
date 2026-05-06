package com.demoform.formengine.service;

import com.demoform.common.dto.PageResult;
import com.demoform.formengine.entity.FormSubmission;
import java.util.List;

/**
 * 表单填报服务接口
 */
public interface FormSubmissionService {

    FormSubmission submit(Long templateId, Long submitterId, String dataJson);

    PageResult<FormSubmission> listByTemplate(int page, int size, Long templateId, Long userId);

    PageResult<FormSubmission> listMySubmissions(int page, int size, Long submitterId);

    List<FormSubmission> exportByTemplate(Long templateId, Long userId);

    FormSubmission findById(Long submissionId);
}
