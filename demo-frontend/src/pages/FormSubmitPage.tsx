import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Form, Input, InputNumber, DatePicker, Select, Radio, Checkbox, Upload, Button, message, Card } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { formTemplateApi, formSubmissionApi } from '../api/form';
import { FormField } from '../types/form';

const FormSubmitPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [template, setTemplate] = useState<any>(null);
  const [fields, setFields] = useState<FormField[]>([]);
  const [form] = Form.useForm();

  useEffect(() => {
    if (id) {
      formTemplateApi.detail(Number(id)).then(t => {
        setTemplate(t);
        try { setFields(JSON.parse(t.schemaJson || '[]')); }
        catch { setFields([]); }
      });
    }
  }, [id]);

  const onFinish = async (values: any) => {
    try {
      await formSubmissionApi.submit(Number(id), JSON.stringify(values));
      message.success('提交成功，待审批后生效');
      navigate('/forms/submit');
    } catch (err: any) {
      message.error(err.message || '提交失败');
    }
  };

  const renderField = (field: FormField) => {
    switch (field.type) {
      case 'text': return <Input placeholder={field.placeholder} />;
      case 'textarea': return <Input.TextArea rows={3} placeholder={field.placeholder} />;
      case 'number': return <InputNumber style={{ width: '100%' }} placeholder={field.placeholder} />;
      case 'date': return <DatePicker style={{ width: '100%' }} />;
      case 'select': return <Select placeholder={field.placeholder}
        options={field.options?.map(o => ({ label: o, value: o }))} />;
      case 'radio': return <Radio.Group options={field.options?.map(o => ({ label: o, value: o }))} />;
      case 'checkbox': return <Checkbox.Group options={field.options?.map(o => ({ label: o, value: o }))} />;
      case 'file': return <Upload><Button icon={<UploadOutlined />}>上传文件</Button></Upload>;
      default: return <Input />;
    }
  };

  if (!template) return null;

  return (
    <Card title={`填报：${template.name}`} style={{ maxWidth: 720, margin: '0 auto' }}>
      <p style={{ color: '#666', marginBottom: 16 }}>{template.description}</p>
      <Form form={form} layout="vertical" onFinish={onFinish}>
        {fields.map(field => (
          <Form.Item key={field.id} name={field.name} label={field.label}
            rules={field.required ? [{ required: true, message: `${field.label}不能为空` }] : []}>
            {renderField(field)}
          </Form.Item>
        ))}
        <Form.Item>
          <Button type="primary" htmlType="submit">提交</Button>
          <Button style={{ marginLeft: 8 }} onClick={() => navigate(-1)}>返回</Button>
        </Form.Item>
      </Form>
    </Card>
  );
};

export default FormSubmitPage;
