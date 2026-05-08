package com.demoform.web.controller;

import com.demoform.common.dto.ApiResponse;
import com.demoform.common.dto.PageResult;
import com.demoform.common.enums.ResultCode;
import com.demoform.common.enums.SubmissionStatus;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.dto.SubmissionRequest;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.formengine.service.FormSubmissionService;
import com.demoform.user.mapper.UserMapper;
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
import java.util.Map;
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
    private final UserMapper userMapper;
    private final FormTemplateMapper templateMapper;
    private final FormSubmissionMapper submissionMapper;

    /** 提交表单数据，自动触发审批流程 */
    @PostMapping("/submissions")
    public ApiResponse<FormSubmission> submit(@Valid @RequestBody SubmissionRequest request,
                                                Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        // 先获取模板信息判断是否需要审批
        FormTemplate template = templateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.FORM_NOT_FOUND);
        }

        FormSubmission submission = submissionService.submit(request.getTemplateId(), userId, request.getDataJson());

        if (Boolean.TRUE.equals(template.getNeedApproval())) {
            // 需要审批 → 启动 Camunda 流程
            approvalService.startApproval(submission.getId());
        } else {
            // 无需审批 → 直接设置为已提交状态
            submission.setStatus(SubmissionStatus.SUBMITTED.name());
            submissionMapper.updateById(submission);
        }

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

    /** 导出填报数据为 CSV（动态列 + 填写人） */
    @GetMapping("/templates/{templateId}/submissions/export")
    public ResponseEntity<byte[]> export(@PathVariable Long templateId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<FormSubmission> submissions = submissionService.exportByTemplate(templateId, userId);

        // Load template to get schema
        FormTemplate template = templateMapper.selectById(templateId);
        List<Map<String, String>> schemaFields = new java.util.ArrayList<>();
        if (template != null && template.getSchemaJson() != null) {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            try {
                var fields = om.readValue(template.getSchemaJson(), List.class);
                for (var f : fields) {
                    Map<String, String> m = (Map<String, String>) f;
                    schemaFields.add(Map.of("name", m.getOrDefault("name", ""), "label", m.getOrDefault("label", m.getOrDefault("name", ""))));
                }
            } catch (Exception e) {
                // fallback: ignore schema
            }
        }

        java.util.function.Function<String, String> esc = v ->
                "\"" + (v == null ? "" : v.replace("\"", "\"\"")) + "\"";

        // Build header: schema field labels + 状态 + 填写人 + 填写时间
        StringBuilder header = new StringBuilder();
        for (var sf : schemaFields) {
            header.append(esc.apply(sf.get("label"))).append(",");
        }
        header.append(esc.apply("状态")).append(",");
        header.append(esc.apply("填写人")).append(",");
        header.append(esc.apply("填写时间"));

        // Build rows
        String csv = submissions.stream().map(s -> {
            Map<String, String> dataMap = new java.util.HashMap<>();
            if (s.getDataJson() != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    var data = om.readValue(s.getDataJson(), java.util.Map.class);
                    for (var entry : (java.util.Set<java.util.Map.Entry>) data.entrySet()) {
                        dataMap.put(String.valueOf(entry.getKey()), entry.getValue() != null ? String.valueOf(entry.getValue()) : "");
                    }
                } catch (Exception e) { /* ignore */ }
            }
            // Look up username via UserMapper
            String username = "";
            if (s.getSubmitterId() != null) {
                var user = userMapper.selectById(s.getSubmitterId());
                if (user != null) username = user.getUsername();
            }
            StringBuilder row = new StringBuilder();
            for (var sf : schemaFields) {
                row.append(esc.apply(dataMap.getOrDefault(sf.get("name"), ""))).append(",");
            }
            row.append(esc.apply(s.getStatus())).append(",");
            row.append(esc.apply(username)).append(",");
            row.append(esc.apply(s.getCreatedAt() != null ? s.getCreatedAt().toString() : ""));
            return row.toString();
        }).collect(Collectors.joining("\r\n"));

        byte[] bytes = (header.toString() + "\r\n" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"submissions_" + templateId + ".csv\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
