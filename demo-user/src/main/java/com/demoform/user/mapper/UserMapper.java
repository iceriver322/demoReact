package com.demoform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.user.dto.UserVO;
import com.demoform.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {

    /** 根据用户名查询用户（含已逻辑删除的记录，用于注册时检查用户名是否已存在） */
    SysUser selectByUsername(@Param("username") String username);

    /** 分页查询用户列表 */
    IPage<UserVO> selectUserPage(Page<UserVO> page, @Param("username") String username);

    /** 根据ID查询用户详情 */
    UserVO selectUserDetail(@Param("userId") Long userId);

    /** 查询用户的角色编码列表 */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /** 更新密码过期日期 */
    int updatePasswordExpireDate(@Param("userId") Long userId,
                                  @Param("expireDate") java.time.LocalDate expireDate);
}
