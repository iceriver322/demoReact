package com.demoform.formengine.service;

import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.formengine.service.impl.FormSubmissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 表单填报服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class FormSubmissionServiceTest {

    @Mock private FormSubmissionMapper submissionMapper;
    @Mock private FormTemplateMapper templateMapper;
    @InjectMocks private FormSubmissionServiceImpl service;

    @Test
    void shouldSubmitSuccessfully() {
        FormTemplate template = new FormTemplate();
        template.setId(1L);
        template.setStatus("PUBLISHED");
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(submissionMapper.insert(any())).thenReturn(1);

        FormSubmission result = service.submit(1L, 1L, "{\"name\":\"test\"}");

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTemplateId()).isEqualTo(1L);
        assertThat(result.getSubmitterId()).isEqualTo(1L);
        verify(submissionMapper).insert(any(FormSubmission.class));
    }

    @Test
    void shouldFailWhenTemplateNotFound() {
        when(templateMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.submit(99L, 1L, "{}"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldFailWhenTemplateNotPublished() {
        FormTemplate template = new FormTemplate();
        template.setId(1L);
        template.setStatus("DRAFT");
        when(templateMapper.selectById(1L)).thenReturn(template);

        assertThatThrownBy(() -> service.submit(1L, 1L, "{}"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldFindById() {
        FormSubmission submission = new FormSubmission();
        submission.setId(1L);
        submission.setStatus("PENDING");
        when(submissionMapper.selectById(1L)).thenReturn(submission);

        FormSubmission result = service.findById(1L);

        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldFailWhenSubmissionNotFound() {
        when(submissionMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BusinessException.class);
    }
}
