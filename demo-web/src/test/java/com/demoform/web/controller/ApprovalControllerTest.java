package com.demoform.web.controller;

import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.service.FormSubmissionService;
import com.demoform.web.filter.JwtAuthFilter;
import com.demoform.web.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 审批控制器测试 —— 自审批校验
 */
@WebMvcTest(ApprovalController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApprovalControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private com.demoform.workflow.service.ApprovalService approvalService;
    @MockBean private FormSubmissionService submissionService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    private final Long userId = 1L;
    private final org.springframework.security.core.Authentication AUTH =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userId, null,
                    AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

    @Test
    void shouldRejectSelfApproval() throws Exception {
        FormSubmission submission = new FormSubmission();
        submission.setId(1L);
        submission.setSubmitterId(1L); // same as userId
        submission.setTemplateId(10L);
        submission.setStatus("PENDING");
        when(submissionService.findById(1L)).thenReturn(submission);

        mockMvc.perform(put("/api/approvals/1/approve")
                        .with(request -> {
                            request.setUserPrincipal(AUTH);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3002))
                .andExpect(jsonPath("$.message").value("不能审批自己的提交"));

        verify(submissionService).findById(1L);
        verifyNoInteractions(approvalService);
    }

    @Test
    void shouldRejectSelfRejection() throws Exception {
        FormSubmission submission = new FormSubmission();
        submission.setId(2L);
        submission.setSubmitterId(1L); // same as userId
        submission.setTemplateId(10L);
        submission.setStatus("PENDING");
        when(submissionService.findById(2L)).thenReturn(submission);

        mockMvc.perform(put("/api/approvals/2/reject")
                        .with(request -> {
                            request.setUserPrincipal(AUTH);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3002))
                .andExpect(jsonPath("$.message").value("不能审批自己的提交"));

        verify(submissionService).findById(2L);
        verifyNoInteractions(approvalService);
    }

    @Test
    void shouldAllowApproveOtherSubmission() throws Exception {
        FormSubmission submission = new FormSubmission();
        submission.setId(3L);
        submission.setSubmitterId(2L); // different from userId
        submission.setTemplateId(10L);
        submission.setStatus("PENDING");
        when(submissionService.findById(3L)).thenReturn(submission);
        when(approvalService.findTaskBySubmissionId(3L)).thenReturn("task1");

        mockMvc.perform(put("/api/approvals/3/approve")
                        .with(request -> {
                            request.setUserPrincipal(AUTH);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(approvalService).approve("task1", 1L);
    }
}
