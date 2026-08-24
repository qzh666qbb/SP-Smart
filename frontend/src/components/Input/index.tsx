import React from 'react';
import { Input as TaroInput, View } from '@tarojs/components';
import styles from './index.module.scss';

export interface InputProps {
  placeholder?: string;
  value?: string;
  type?: 'text' | 'number' | 'idcard' | 'digit' | 'safe-password' | 'nickname' | 'text-password';
  onChange?: (value: string) => void;
  disabled?: boolean;
  className?: string;
}

const Input: React.FC<InputProps> = ({
  placeholder,
  value,
  type = 'text',
  onChange,
  disabled,
  className,
  ...rest
}) => {
  return (
    <View className={`${styles.wrap} ${className || ''}`}>
      <TaroInput
        className={styles.input}
        disabled={disabled}
        type={type}
        placeholder={placeholder}
        value={value}
        onInput={(e) => onChange && onChange(String(e.detail.value ?? ''))}
        {...(rest as any)}
      />
    </View>
  );
};

export default Input;
