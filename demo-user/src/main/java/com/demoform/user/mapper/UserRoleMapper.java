package com.demoform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demoform.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /** 批量插入用户角色关联 */
    int insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /** 删除用户的所有角色 */
    int deleteByUserId(@Param("userId") Long userId);
}
