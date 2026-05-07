import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Table, Tag, Button, Modal, Form, Input, InputNumber,
  DatePicker, Select, Radio, Checkbox, Upload, message, Card, Space, Descriptions
} from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { formTemplateApi, formSubmissionApi, FormTemplate, FormSubmission } from '../api/form';
import type { FormField } from '../types/form';
import dayjs from 'dayjs';

const statusColor: Record<string, string> = {
  PENDING: 'orange', APPROVED: 'green', REJECTED: 'red',
};

const statusText: Record<string, string> = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回',
};

const FormSubmitPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [template, setTemplate] = useState<FormTemplate | null>(null);
  const [submissions, setSubmissions] = useState<FormSubmission[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalLoading, setModalLoading] = useState(false);
  const [editRecord, setEditRecord] = useState<FormSubmission | null>(null);
  const [schemaFields, setSchemaFields] = useState<FormField[]>([]);

  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const t = await formTemplateApi.detail(Number(id));
      setTemplate(t);
      const fields: FormField[] = JSON.parse(t.schemaJson || '[]');
      setSchemaFields(fields);
      const mySubs = await formSubmissionApi.listMyByTemplate(Number(id));
      setSubmissions(mySubs);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, [id]);

  const renderField = (field: FormField) => {
    const required = field.required ? { required: true, message: `${field.label}不能为空` } : undefined;
    switch (field.type) {
      case 'text':
        return <Input placeholder={field.placeholder} />;
      case 'textarea':
        return <Input.TextArea rows={3} placeholder={field.placeholder} />;
      case 'number':
        return <InputNumber style={{ width: '100%' }} placeholder={field.placeholder} />;
      case 'date':
        return <DatePicker style={{ width: '100%' }} />;
      case 'select':
        return (
          <Select placeholder={field.placeholder}>
            {field.options?.map(o => <Select.Option key={o} value={o}>{o}</Select.Option>)}
          </Select>
        );
      case 'radio':
        return (
          <Radio.Group>
            {field.options?.map(o => <Radio key={o} value={o}>{o}</Radio>)}
          </Radio.Group>
        );
      case 'checkbox':
        return (
          <Checkbox.Group>
            <Space direction="vertical">
              {field.options?.map(o => <Checkbox key={o} value={o}>{o}</Checkbox>)}
            </Space>
          </Checkbox.Group>
        );
      case 'file':
        return <Upload><Button>上传文件</Button></Upload>;
      default:
        return <Input />;
    }
  };

  // Map dayjs objects to strings before submitting
  const formatValues = (values: Record<string, any>, fields: FormField[]): Record<string, any> => {
    const result: Record<string, any> = {};
    for (const f of fields) {
      const v = values[f.name];
      if (f.type === 'date' && dayjs.isDayjs(v)) {
        result[f.name] = v.format('YYYY-MM-DD');
      } else if (f.type === 'checkbox' && Array.isArray(v)) {
        result[f.name] = v.join(', ');
      } else {
        result[f.name] = v;
      }
    }
    return result;
  };

  const handleOpenNew = () => {
    setEditRecord(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleOpenEdit = (record: FormSubmission) => {
    setEditRecord(record);
    const data = JSON.parse(record.dataJson || '{}');
    // Parse dates back to dayjs for DatePicker
    const initValues: Record<string, any> = {};
    for (const f of schemaFields) {
      const val = data[f.name];
      if (f.type === 'date' && val) {
        initValues[f.name] = dayjs(val);
      } else {
        initValues[f.name] = val;
      }
    }
    form.setFieldsValue(initValues);
    setModalVisible(true);
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      setModalLoading(true);
      const formatted = formatValues(values, schemaFields);
      await formSubmissionApi.submit(Number(id!), JSON.stringify(formatted));
      message.success('提交成功，待审批后生效');
      setModalVisible(false);
      form.resetFields();
      loadData();
    } catch (err: any) {
      if (err.errorFields) return; // validation error
      message.error(err?.message || '提交失败');
    } finally {
      setModalLoading(false);
    }
  };

  // Build dynamic columns for history table
  const dynamicColumns = schemaFields.map(f => ({
    title: f.label || f.name,
    dataIndex: f.name,
    width: 120,
    render: (_: any, record: FormSubmission) => {
      const data = JSON.parse(record.dataJson || '{}');
      const val = data[f.name];
      if (val === undefined || val === null) return '-';
      if (f.type === 'date') return val;
      if (Array.isArray(val)) return val.join(', ');
      return String(val);
    },
  }));

  const columns = [
    ...dynamicColumns,
    { title: '状态', dataIndex: 'status', width: 100,
      render: (s: string) => <Tag color={statusColor[s]}>{statusText[s] || s}</Tag> },
    { title: '操作', width: 100,
      render: (_: any, record: FormSubmission) => (
        <Button type="link" size="small" icon={<EditOutlined />}
          onClick={() => handleOpenEdit(record)}>修改</Button>
      )},
  ];

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Descriptions column={1}>
          <Descriptions.Item label="表单名称">
            <span style={{ fontSize: 18, fontWeight: 'bold' }}>{template?.name}</span>
          </Descriptions.Item>
          <Descriptions.Item label="描述">{template?.description || '无'}</Descriptions.Item>
        </Descriptions>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleOpenNew}>
          新增提交
        </Button>
      </Card>

      <h3 style={{ marginBottom: 16 }}>提交历史</h3>
      <Table columns={columns} dataSource={submissions}
        rowKey="id" loading={loading} pagination={false} scroll={{ x: 'max-content' }} />

      <Modal
        title={editRecord ? '修改提交' : '新增提交'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => { setModalVisible(false); form.resetFields(); }}
        confirmLoading={modalLoading}
        width={640}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          {schemaFields.map(f => (
            <Form.Item key={f.id || f.name} name={f.name} label={f.label}
              rules={f.required ? [{ required: true, message: `${f.label}不能为空` }] : undefined}>
              {renderField(f)}
            </Form.Item>
          ))}
        </Form>
      </Modal>
    </div>
  );
};

export default FormSubmitPage;
