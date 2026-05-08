package com.demoform.workflow.service.impl;

import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.workflow.dto.TaskDto;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock private RuntimeService runtimeService;
    @Mock private TaskService taskService;
    @Mock private FormSubmissionMapper submissionMapper;
    @Mock private FormTemplateMapper templateMapper;
    @InjectMocks private ApprovalServiceImpl approvalService;

    @Test
    void shouldReturnPendingTasksWithSubmissionData() {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn("task1");
        when(task.getProcessInstanceId()).thenReturn("proc1");
        when(task.getName()).thenReturn("审批");
        when(task.getCreateTime()).thenReturn(new Date());

        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskQuery.initializeFormKeys()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(taskService.createTaskQuery()).thenReturn(taskQuery);

        Map<String, Object> variables = new HashMap<>();
        // Camunda 7 默认将小整数存为 Integer，使用 Number.longValue() 兼容
        variables.put("submissionId", 1);
        when(taskService.getVariables("task1")).thenReturn(variables);

        FormSubmission submission = new FormSubmission();
        submission.setId(1L);
        submission.setTemplateId(10L);
        submission.setSubmitterId(1L);
        submission.setDataJson("{\"name\":\"张三\",\"age\":30}");
        submission.setStatus("PENDING");
        when(submissionMapper.selectById(1L)).thenReturn(submission);

        FormTemplate template = new FormTemplate();
        template.setId(10L);
        template.setName("员工信息表");
        template.setSchemaJson("[{\"name\":\"name\",\"label\":\"姓名\"}]");
        when(templateMapper.selectById(10L)).thenReturn(template);

        List<TaskDto> result = approvalService.getPendingTasks(2L);

        assertThat(result).hasSize(1);
        TaskDto dto = result.get(0);
        assertThat(dto.getTaskId()).isEqualTo("task1");
        assertThat(dto.getSubmissionData()).isEqualTo("{\"name\":\"张三\",\"age\":30}");
        assertThat(dto.getTemplateName()).isEqualTo("员工信息表");
        assertThat(dto.getSchemaJson()).isEqualTo("[{\"name\":\"name\",\"label\":\"姓名\"}]");
    }

    @Test
    void shouldFilterOutSelfSubmissions() {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn("task1");

        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskQuery.initializeFormKeys()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(taskService.createTaskQuery()).thenReturn(taskQuery);

        Map<String, Object> variables = new HashMap<>();
        variables.put("submissionId", 1);
        when(taskService.getVariables("task1")).thenReturn(variables);

        FormSubmission submission = new FormSubmission();
        submission.setId(1L);
        submission.setTemplateId(10L);
        submission.setSubmitterId(1L);
        submission.setDataJson("{\"name\":\"张三\",\"age\":30}");
        submission.setStatus("PENDING");
        when(submissionMapper.selectById(1L)).thenReturn(submission);

        // userId matches submitterId → should be filtered out
        List<TaskDto> result = approvalService.getPendingTasks(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleTaskWithoutSubmission() {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn("task2");
        when(task.getProcessInstanceId()).thenReturn("proc2");
        when(task.getName()).thenReturn("审批");
        when(task.getCreateTime()).thenReturn(new Date());

        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskQuery.initializeFormKeys()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(taskService.createTaskQuery()).thenReturn(taskQuery);

        Map<String, Object> variables = new HashMap<>();
        variables.put("submissionId", 99L);
        when(taskService.getVariables("task2")).thenReturn(variables);

        when(submissionMapper.selectById(99L)).thenReturn(null);

        List<TaskDto> result = approvalService.getPendingTasks(2L);

        assertThat(result).hasSize(1);
        TaskDto dto = result.get(0);
        assertThat(dto.getSubmissionData()).isNull();
        assertThat(dto.getTemplateName()).isNull();
        assertThat(dto.getSchemaJson()).isNull();
    }
}
