package com.demoform.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 角色分配请求
 */
@Data
public class RoleAssignRequest {
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}
