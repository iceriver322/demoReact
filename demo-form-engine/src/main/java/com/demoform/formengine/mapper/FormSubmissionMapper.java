package com.demoform.formengine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demoform.formengine.entity.FormSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 表单填报数据 Mapper
 */
@Mapper
public interface FormSubmissionMapper extends BaseMapper<FormSubmission> {

    /** 分页查询指定模板的填报数据 */
    IPage<FormSubmission> selectByTemplateId(Page<FormSubmission> page,
                                              @Param("templateId") Long templateId);

    /** 分页查询我的填报记录 */
    IPage<FormSubmission> selectMySubmissions(Page<FormSubmission> page,
                                               @Param("submitterId") Long submitterId);

    /** 查询指定模板的所有填报数据(不分页，用于导出CSV) */
    List<FormSubmission> selectAllByTemplateId(@Param("templateId") Long templateId);

    /** 查询当前用户在某模板下的所有提交记录 */
    List<FormSubmission> selectMyByTemplateId(@Param("templateId") Long templateId,
                                               @Param("submitterId") Long submitterId);
}
