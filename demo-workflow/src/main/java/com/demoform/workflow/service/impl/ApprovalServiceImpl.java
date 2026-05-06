package com.demoform.workflow.service.impl;

import com.demoform.common.enums.ResultCode;
import com.demoform.common.enums.SubmissionStatus;
import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.entity.FormSubmission;
import com.demoform.formengine.mapper.FormSubmissionMapper;
import com.demoform.workflow.dto.TaskDto;
import com.demoform.workflow.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批服务实现 —— 基于 Camunda 7 嵌入式流程引擎
 */
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final FormSubmissionMapper submissionMapper;

    @Override
    @Transactional
    public void startApproval(Long submissionId) {
        FormSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCode.SUBMISSION_NOT_FOUND);
        }
        if (!SubmissionStatus.PENDING.name().equals(submission.getStatus())) {
            throw new BusinessException(ResultCode.ALREADY_APPROVED);
        }
        // 启动审批流程，以 submissionId 作为 businessKey
        Map<String, Object> variables = new HashMap<>();
        variables.put("submissionId", submissionId);
        runtimeService.startProcessInstanceByKey(
                "approval-process", String.valueOf(submissionId), variables);
    }

    @Override
    @Transactional
    public void approve(String taskId, Long approverId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.APPROVAL_TASK_NOT_FOUND);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("approverId", approverId);
        taskService.complete(taskId, variables);
    }

    @Override
    @Transactional
    public void reject(String taskId, Long approverId, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.APPROVAL_TASK_NOT_FOUND);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);
        variables.put("approverId", approverId);
        if (reason != null) variables.put("rejectReason", reason);
        taskService.complete(taskId, variables);
    }

    @Override
    public List<TaskDto> getPendingTasks() {
        List<Task> tasks = taskService.createTaskQuery()
                .initializeFormKeys()
                .list();
        return tasks.stream().map(task -> TaskDto.builder()
                .taskId(task.getId())
                .processInstanceId(task.getProcessInstanceId())
                .name(task.getName())
                .createTime(task.getCreateTime())
                .variables(taskService.getVariables(task.getId()))
                .build()).collect(Collectors.toList());
    }

    @Override
    public String findTaskBySubmissionId(Long submissionId) {
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(String.valueOf(submissionId))
                .singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.APPROVAL_TASK_NOT_FOUND);
        }
        return task.getId();
    }
}
