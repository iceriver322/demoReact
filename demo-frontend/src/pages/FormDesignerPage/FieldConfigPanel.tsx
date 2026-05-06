import { Form, Input, Switch } from 'antd';
import { FormField } from '../../types/form';

interface Props {
  field: FormField | null;
  onUpdate: (field: FormField) => void;
}

const FieldConfigPanel: React.FC<Props> = ({ field, onUpdate }) => {
  if (!field) {
    return (
      <div>
        <h4>属性配置</h4>
        <p style={{ color: '#999' }}>请选择一个字段</p>
      </div>
    );
  }

  const handleChange = (changed: Partial<FormField>) => {
    onUpdate({ ...field, ...changed });
  };

  return (
    <div>
      <h4 style={{ marginBottom: 12 }}>属性配置</h4>
      <Form layout="vertical" size="small">
        <Form.Item label="字段标签">
          <Input value={field.label}
            onChange={e => handleChange({ label: e.target.value })} />
        </Form.Item>
        <Form.Item label="字段名（key）">
          <Input value={field.name}
            onChange={e => handleChange({ name: e.target.value })} />
        </Form.Item>
        <Form.Item label="占位提示">
          <Input value={field.placeholder || ''}
            onChange={e => handleChange({ placeholder: e.target.value })} />
        </Form.Item>
        <Form.Item label="是否必填">
          <Switch checked={field.required}
            onChange={v => handleChange({ required: v })} />
        </Form.Item>
        {(field.type === 'select' || field.type === 'radio' || field.type === 'checkbox') && (
          <Form.Item label="选项（每行一个）">
            <Input.TextArea
              value={field.options?.join('\n') || ''}
              rows={4}
              onChange={e => handleChange({ options: e.target.value.split('\n').filter(s => s.trim()) })}
            />
          </Form.Item>
        )}
      </Form>
    </div>
  );
};

export default FieldConfigPanel;
