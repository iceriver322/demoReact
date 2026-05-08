import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Row, Col, Input, Button, Space, Switch, message } from 'antd';
import { SaveOutlined, SendOutlined } from '@ant-design/icons';
import {
  DndContext, DragEndEvent, PointerSensor, useSensor, useSensors,
  closestCenter,
} from '@dnd-kit/core';
import {
  SortableContext, verticalListSortingStrategy, arrayMove,
} from '@dnd-kit/sortable';
import FieldPalette from './FormDesignerPage/FieldPalette';
import DesignCanvas from './FormDesignerPage/DesignCanvas';
import FieldConfigPanel from './FormDesignerPage/FieldConfigPanel';
import { FormField } from '../types/form';
import { formTemplateApi } from '../api/form';

let idCounter = 0;
const genId = () => `field_${Date.now()}_${idCounter++}`;

const FormDesignerPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [needApproval, setNeedApproval] = useState(true);
  const [fields, setFields] = useState<FormField[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const sensors = useSensors(useSensor(PointerSensor, {
    activationConstraint: { distance: 5 },
  }));

  useEffect(() => {
    if (id) {
      formTemplateApi.detail(Number(id)).then((t) => {
        setName(t.name);
        setDescription(t.description || '');
        setNeedApproval(t.needApproval !== false);
        try { setFields(JSON.parse(t.schemaJson || '[]')); }
        catch { setFields([]); }
      }).catch(() => message.error('表单不存在'));
    }
  }, [id]);

  const handleDragEnd = useCallback((event: DragEndEvent) => {
    const { active, over } = event;
    if (!over) return;

    if (active.data.current?.isNew) {
      const newField: FormField = {
        id: genId(),
        type: active.data.current.type,
        label: '新字段',
        name: `field_${fields.length + 1}`,
        required: false,
      };
      setFields(prev => [...prev, newField]);
      setSelectedId(newField.id);
      return;
    }

    const oldIdx = fields.findIndex(f => f.id === active.id);
    const newIdx = fields.findIndex(f => f.id === over.id);
    if (oldIdx !== -1 && newIdx !== -1 && oldIdx !== newIdx) {
      setFields(prev => arrayMove(prev, oldIdx, newIdx));
    }
  }, [fields]);

  const selectedField = fields.find(f => f.id === selectedId) || null;

  const handleFieldUpdate = (updated: FormField) => {
    setFields(prev => prev.map(f => f.id === updated.id ? updated : f));
  };

  const handleSave = async () => {
    if (!name.trim()) { message.warning('请输入表单名称'); return; }
    const schema = JSON.stringify(fields);
    try {
      if (isEdit) {
        await formTemplateApi.update(Number(id), { name, description, schemaJson: schema, needApproval });
        message.success('保存成功');
      } else {
        const created = await formTemplateApi.create({ name, description, schemaJson: schema, needApproval });
        message.success('创建成功');
        navigate(`/forms/templates/${created.id}`, { replace: true });
      }
    } catch (err: any) {
      message.error(err.message || '保存失败');
    }
  };

  const handlePublish = async () => {
    if (!isEdit || !id) return;
    await handleSave();
    try {
      await formTemplateApi.publish(Number(id));
      message.success('发布成功');
      navigate('/forms/templates');
    } catch (err: any) {
      message.error(err.message || '发布失败');
    }
  };

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Space>
          <Input placeholder="表单名称" value={name}
            onChange={e => setName(e.target.value)} style={{ width: 240 }} />
          <Input placeholder="表单描述" value={description}
            onChange={e => setDescription(e.target.value)} style={{ width: 240 }} />
        </Space>
        <Space>
          <span>需要审批</span>
          <Switch checked={needApproval} onChange={setNeedApproval} />
        </Space>
        <Space>
          <Button icon={<SaveOutlined />} onClick={handleSave}>保存</Button>
          <Button onClick={() => navigate(-1)}>取消</Button>
          {isEdit && (
            <Button type="primary" icon={<SendOutlined />} onClick={handlePublish}>发布</Button>
          )}
        </Space>
      </Space>

      <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
        <SortableContext items={fields.map(f => f.id)} strategy={verticalListSortingStrategy}>
          <Row gutter={16}>
            <Col span={4}><FieldPalette /></Col>
            <Col span={14}>
              <DesignCanvas
                fields={fields}
                selectedId={selectedId}
                onSelect={setSelectedId}
                onRemove={(fid) => {
                  setFields(prev => prev.filter(f => f.id !== fid));
                  if (selectedId === fid) setSelectedId(null);
                }}
              />
            </Col>
            <Col span={6}>
              <FieldConfigPanel field={selectedField} onUpdate={handleFieldUpdate} />
            </Col>
          </Row>
        </SortableContext>
      </DndContext>
    </div>
  );
};

export default FormDesignerPage;
