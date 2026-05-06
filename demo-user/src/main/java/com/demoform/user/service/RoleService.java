package com.demoform.user.service;

import com.demoform.user.entity.SysRole;

/**
 * 角色服务接口
 */
public interface RoleService {

    /** 根据角色编码查询角色 */
    SysRole findByCode(String code);
}
