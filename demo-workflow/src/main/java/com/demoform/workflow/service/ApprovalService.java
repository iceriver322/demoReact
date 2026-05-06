package com.demoform.workflow.service;

import com.demoform.workflow.dto.TaskDto;
import java.util.List;

/**
 * 审批服务接口 —— 封装 Camunda 7 流程操作
 */
public interface ApprovalService {

    /** 启动审批流程 */
    void startApproval(Long submissionId);

    /** 批准 */
    void approve(String taskId, Long approverId);

    /** 驳回 */
    void reject(String taskId, Long approverId, String reason);

    /** 查询待审批任务列表 */
    List<TaskDto> getPendingTasks();

    /** 根据业务ID查找任务ID */
    String findTaskBySubmissionId(Long submissionId);
}
