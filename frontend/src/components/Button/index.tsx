import React from 'react';
import { Button } from '@tarojs/components';
import styles from './index.module.scss';

interface ButtonProps {
  children: React.ReactNode;
  type?: 'primary' | 'secondary' | 'warning' | 'danger' | 'plain';
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  loading?: boolean;
  block?: boolean;
  onClick?: () => void;
  className?: string;
}

export default function CustomButton({ 
  children, 
  type = 'primary', 
  size = 'md', 
  disabled = false,
  loading = false,
  block = false,
  onClick,
  className
}: ButtonProps) {
  const sizeMap = {
    sm: styles.sizeSm,
    md: styles.sizeMd,
    lg: styles.sizeLg
  };
  
  const typeMap: Record<string, string> = {
    primary: styles.typePrimary,
    secondary: styles.typeSecondary,
    warning: styles.typeWarning,
    danger: styles.typeDanger,
    plain: styles.typePlain || ''
  };

  const blockClass = block ? styles.block : '';
  const isDisabled = disabled || loading;
  
  return (
    <Button
      className={`${styles.button} ${sizeMap[size]} ${typeMap[type]} ${blockClass} ${isDisabled ? styles.disabled : ''} ${loading ? styles.loading : ''} ${className || ''}`}
      disabled={isDisabled}
      onClick={onClick}
      style={block ? { display: 'block', width: '100%' } : {}}
    >
      {loading ? '加载中...' : children}
    </Button>
  );
}
