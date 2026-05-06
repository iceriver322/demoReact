package com.demoform.workflow.delegate;

import com.demoform.common.enums.SubmissionStatus;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Camunda 审批完成回调 Delegate —— 流程结束时更新填报数据状态
 */
@Component
@RequiredArgsConstructor
public class ApprovalCompleteDelegate implements JavaDelegate {

    private final FormSubmissionMapper submissionMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long submissionId = (Long) execution.getVariable("submissionId");
        Long approverId = (Long) execution.getVariable("approverId");
        Boolean approved = (Boolean) execution.getVariable("approved");

        FormSubmission submission = submissionMapper.selectById(submissionId);
        if (submission != null) {
            submission.setApproverId(approverId);
            submission.setApprovedAt(LocalDateTime.now());
            submission.setStatus(approved ? SubmissionStatus.APPROVED.name()
                    : SubmissionStatus.REJECTED.name());
            submissionMapper.updateById(submission);
        }
    }
}
