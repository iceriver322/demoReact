package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.service.FormSubmissionService;
import com.demoform.workflow.dto.TaskDto;
import com.demoform.workflow.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批控制器 —— 特权用户/管理员操作
 */
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PRIVILEGED')")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final FormSubmissionService submissionService;

    /** 待审批列表 */
    @GetMapping("/pending")
    public ApiResponse<List<TaskDto>> pending() {
        return ApiResponse.success(approvalService.getPendingTasks());
    }

    /** 批准 */
    @PutMapping("/{submissionId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long submissionId, Authentication auth) {
        Long approverId = (Long) auth.getPrincipal();
        FormSubmission submission = submissionService.findById(submissionId);
        if (submission.getSubmitterId().equals(approverId)) {
            throw new BusinessException(ResultCode.CANNOT_APPROVE_SELF);
        }
        String taskId = approvalService.findTaskBySubmissionId(submissionId);
        approvalService.approve(taskId, approverId);
        return ApiResponse.success();
    }

    /** 驳回 */
    @PutMapping("/{submissionId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long submissionId,
                                      @RequestParam(required = false) String reason,
                                      Authentication auth) {
        Long approverId = (Long) auth.getPrincipal();
        FormSubmission submission = submissionService.findById(submissionId);
        if (submission.getSubmitterId().equals(approverId)) {
            throw new BusinessException(ResultCode.CANNOT_APPROVE_SELF);
        }
        String taskId = approvalService.findTaskBySubmissionId(submissionId);
        approvalService.reject(taskId, approverId, reason);
        return ApiResponse.success();
    }
}
