import { useDroppable } from '@dnd-kit/core';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { FormField } from '../../types/form';
import { DeleteOutlined, HolderOutlined } from '@ant-design/icons';
import { Button } from 'antd';

interface Props {
  fields: FormField[];
  selectedId: string | null;
  onSelect: (id: string) => void;
  onRemove: (id: string) => void;
}

const DesignCanvas: React.FC<Props> = ({ fields, selectedId, onSelect, onRemove }) => {
  const { setNodeRef, isOver } = useDroppable({ id: 'canvas' });

  return (
    <div>
      <h4 style={{ marginBottom: 12 }}>表单预览</h4>
      <div
        ref={setNodeRef}
        style={{
          minHeight: 400,
          background: isOver ? '#f0f5ff' : '#fff',
          border: '2px dashed ' + (isOver ? '#1677ff' : '#d9d9d9'),
          borderRadius: 8,
          padding: 16,
        }}
      >
        {fields.length === 0 && (
          <div style={{ textAlign: 'center', color: '#999', paddingTop: 160 }}>
            从左侧拖入字段到此处
          </div>
        )}
        {fields.map((field) => (
          <SortableField
            key={field.id}
            field={field}
            isSelected={selectedId === field.id}
            onSelect={() => onSelect(field.id)}
            onRemove={() => onRemove(field.id)}
          />
        ))}
      </div>
    </div>
  );
};

const SortableField: React.FC<{
  field: FormField;
  isSelected: boolean;
  onSelect: () => void;
  onRemove: () => void;
}> = ({ field, isSelected, onSelect, onRemove }) => {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({
    id: field.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    padding: '8px 12px',
    marginBottom: 8,
    background: isSelected ? '#e6f7ff' : '#fafafa',
    border: `1px solid ${isSelected ? '#1677ff' : '#d9d9d9'}`,
    borderRadius: 6,
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    cursor: 'pointer',
  };

  return (
    <div ref={setNodeRef} style={style} onClick={onSelect}>
      <span {...attributes} {...listeners} style={{ cursor: 'grab' }}>
        <HolderOutlined />
      </span>
      <span style={{ flex: 1 }}>
        {field.label}
        {field.required && <span style={{ color: 'red', marginLeft: 4 }}>*</span>}
      </span>
      <span style={{ color: '#999', fontSize: 12 }}>{field.type}</span>
      <Button
        type="text" size="small" danger
        icon={<DeleteOutlined />}
        onClick={(e) => { e.stopPropagation(); onRemove(); }}
      />
    </div>
  );
};

export default DesignCanvas;
