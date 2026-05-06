package com.demoform.formengine.service;

import com.demoform.common.exception.BusinessException;
import com.demoform.formengine.dto.TemplateCreateRequest;
import com.demoform.formengine.entity.FormTemplate;
import com.demoform.formengine.mapper.FormTemplateMapper;
import com.demoform.formengine.service.impl.FormTemplateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 表单模板服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class FormTemplateServiceTest {

    @Mock private FormTemplateMapper formTemplateMapper;
    @InjectMocks private FormTemplateServiceImpl service;

    @Test
    void shouldCreateTemplate() {
        when(formTemplateMapper.insert(any())).thenReturn(1);
        TemplateCreateRequest req = new TemplateCreateRequest();
        req.setName("测试表单");
        req.setDescription("描述");
        req.setSchemaJson("[{\"name\":\"field1\",\"type\":\"text\"}]");

        FormTemplate result = service.create(1L, req);

        assertThat(result.getName()).isEqualTo("测试表单");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(formTemplateMapper).insert(any(FormTemplate.class));
    }

    @Test
    void shouldPublishTemplate() {
        FormTemplate template = new FormTemplate();
        template.setId(1L);
        template.setOwnerId(1L);
        template.setStatus("DRAFT");
        when(formTemplateMapper.selectById(1L)).thenReturn(template);
        when(formTemplateMapper.updateById(any())).thenReturn(1);

        service.publish(1L, 1L);

        assertThat(template.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    void shouldFailWhenNotOwner() {
        FormTemplate template = new FormTemplate();
        template.setId(1L);
        template.setOwnerId(2L); // 所有者是 2，操作者是 1
        when(formTemplateMapper.selectById(1L)).thenReturn(template);

        assertThatThrownBy(() -> service.publish(1L, 1L))
                .isInstanceOf(BusinessException.class);
    }
}
