export interface FormField {
  id: string;
  type: 'text' | 'textarea' | 'number' | 'date' | 'select' | 'radio' | 'checkbox' | 'file';
  label: string;
  name: string;
  required: boolean;
  placeholder?: string;
  options?: string[];
}

export const fieldTypeLabels: Record<string, string> = {
  text: '文本输入',
  textarea: '多行文本',
  number: '数字输入',
  date: '日期选择',
  select: '下拉选择',
  radio: '单选',
  checkbox: '多选',
  file: '文件上传',
};
