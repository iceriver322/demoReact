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

    /** 查询当前用户在某模板下的所有提交记录 */
    List<FormSubmission> listMyByTemplateId(Long templateId, Long submitterId);

    List<FormSubmission> exportByTemplate(Long templateId, Long userId);

    FormSubmission findById(Long submissionId);

    /** 将提交流设置为直接提交状态（无需审批） */
    void markAsSubmitted(Long submissionId);
}
