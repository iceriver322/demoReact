package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.dto.PageResult;
import com.demoform.formengine.dto.SubmissionRequest;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.service.FormSubmissionService;
import com.demoform.workflow.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表单填报控制器 —— 提交数据、查看填报记录、导出 CSV
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormSubmissionController {

    private final FormSubmissionService submissionService;
    private final ApprovalService approvalService;

    /** 提交表单数据，自动触发审批流程 */
    @PostMapping("/submissions")
    public ApiResponse<FormSubmission> submit(@Valid @RequestBody SubmissionRequest request,
                                                Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        FormSubmission submission = submissionService.submit(request.getTemplateId(), userId, request.getDataJson());
        // 触发审批流程
        approvalService.startApproval(submission.getId());
        return ApiResponse.success(submission);
    }

    /** 查询当前用户在某模板下的所有提交记录（用于"填报数据"页面历史展示） */
    @GetMapping("/submissions/my/template/{templateId}")
    public ApiResponse<List<FormSubmission>> listMyByTemplate(
            @PathVariable Long templateId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(submissionService.listMyByTemplateId(templateId, userId));
    }

    /** 我的填报记录 */
    @GetMapping("/submissions/my")
    public ApiResponse<PageResult<FormSubmission>> listMy(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(submissionService.listMySubmissions(page, size, userId));
    }

    /** 查看某模板的填报数据（表单所有者） */
    @GetMapping("/templates/{templateId}/submissions")
    public ApiResponse<PageResult<FormSubmission>> listByTemplate(
            @PathVariable Long templateId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(submissionService.listByTemplate(page, size, templateId, userId));
    }

    /** 导出填报数据为 CSV */
    @GetMapping("/templates/{templateId}/submissions/export")
    public ResponseEntity<byte[]> export(@PathVariable Long templateId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<FormSubmission> submissions = submissionService.exportByTemplate(templateId, userId);
        // CSV 转义：将值用双引号包裹，内部双引号重复
        java.util.function.Function<String, String> esc = v ->
                "\"" + (v == null ? "" : v.replace("\"", "\"\"")) + "\"";
        String csv = submissions.stream()
                .map(s -> esc.apply(String.valueOf(s.getId())) + ","
                        + esc.apply(String.valueOf(s.getSubmitterId())) + ","
                        + esc.apply(s.getDataJson()) + ","
                        + esc.apply(s.getStatus()) + ","
                        + esc.apply(String.valueOf(s.getCreatedAt())))
                .collect(Collectors.joining("\r\n"));
        byte[] bytes = ("id,submitterId,data,status,createdAt\r\n" + csv)
                .getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"submissions_" + templateId + ".csv\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
