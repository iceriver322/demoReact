package com.demoform.formengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.formengine.entity.FormTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 表单模板 Mapper
 */
@Mapper
public interface FormTemplateMapper extends BaseMapper<FormTemplate> {

    /** 分页查询我的表单 */
    IPage<FormTemplate> selectMyTemplates(Page<FormTemplate> page, @Param("ownerId") Long ownerId);

    /** 查询已发布的表单列表（供用户填报） */
    IPage<FormTemplate> selectPublishedTemplates(Page<FormTemplate> page);
}
