import React from 'react';
import { Descriptions } from 'antd';

export interface SubmissionDataDisplayProps {
  schemaJson: string;
  dataJson: string;
  extra?: { label: string; value: React.ReactNode }[];
}

interface SchemaField {
  name: string;
  label: string;
  type?: string;
}

const SubmissionDataDisplay: React.FC<SubmissionDataDisplayProps> = ({ schemaJson, dataJson, extra }) => {
  let schemaFields: SchemaField[] = [];
  try {
    schemaFields = JSON.parse(schemaJson || '[]');
  } catch { /* ignore invalid JSON */ }

  let data: Record<string, any> = {};
  try {
    data = JSON.parse(dataJson || '{}');
  } catch { /* ignore invalid JSON */ }

  const items: { label: string; value: React.ReactNode }[] = [];

  for (const field of schemaFields) {
    const val = data[field.name];
    let displayVal: React.ReactNode = '-';
    if (val !== undefined && val !== null) {
      if (Array.isArray(val)) {
        displayVal = val.join(', ');
      } else {
        displayVal = String(val);
      }
    }
    items.push({ label: field.label || field.name, value: displayVal });
  }

  if (extra) {
    items.push(...extra);
  }

  return (
    <Descriptions column={1} size="small" bordered>
      {items.map((item, idx) => (
        <Descriptions.Item key={idx} label={item.label}>{item.value}</Descriptions.Item>
      ))}
    </Descriptions>
  );
};

export default SubmissionDataDisplay;
