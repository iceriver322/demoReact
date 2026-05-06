import { useDraggable } from '@dnd-kit/core';
import { fieldTypeLabels } from '../../types/form';

const fieldTypes: { type: string; icon: string }[] = [
  { type: 'text', icon: 'Aa' },
  { type: 'textarea', icon: '📝' },
  { type: 'number', icon: '#' },
  { type: 'date', icon: '📅' },
  { type: 'select', icon: '📋' },
  { type: 'radio', icon: '🔘' },
  { type: 'checkbox', icon: '☑' },
  { type: 'file', icon: '📎' },
];

const FieldPalette: React.FC = () => {
  return (
    <div>
      <h4 style={{ marginBottom: 12 }}>字段类型</h4>
      {fieldTypes.map((ft) => (
        <PaletteItem key={ft.type} type={ft.type} icon={ft.icon} />
      ))}
    </div>
  );
};

const PaletteItem: React.FC<{ type: string; icon: string }> = ({ type, icon }) => {
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `palette-${type}`,
    data: { type, isNew: true },
  });

  return (
    <div
      ref={setNodeRef}
      {...listeners}
      {...attributes}
      style={{
        padding: '8px 12px',
        marginBottom: 8,
        background: isDragging ? '#e6f7ff' : '#fafafa',
        border: '1px solid #d9d9d9',
        borderRadius: 6,
        cursor: 'grab',
        userSelect: 'none',
      }}
    >
      {icon} {(fieldTypeLabels as any)[type] || type}
    </div>
  );
};

export default FieldPalette;
