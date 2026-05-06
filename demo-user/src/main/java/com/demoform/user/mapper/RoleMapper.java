package com.demoform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demoform.user.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<SysRole> {

    /** 根据角色编码查询角色 */
    SysRole selectByCode(@Param("code") String code);
}
