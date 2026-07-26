import React from 'react';
import { Button } from '@tarojs/components';
import styles from './index.module.scss';

interface ButtonProps {
  children: React.ReactNode;
  type?: 'primary' | 'secondary' | 'warning' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  onClick?: () => void;
  className?: string;
}

export default function CustomButton({ 
  children, 
  type = 'primary', 
  size = 'md', 
  disabled = false,
  onClick,
  className
}: ButtonProps) {
  const sizeMap = {
    sm: styles.sizeSm,
    md: styles.sizeMd,
    lg: styles.sizeLg
  };
  
  const typeMap = {
    primary: styles.typePrimary,
    secondary: styles.typeSecondary,
    warning: styles.typeWarning,
    danger: styles.typeDanger
  };
  
  return (
    <Button
      className={`${styles.button} ${sizeMap[size]} ${typeMap[type]} ${disabled ? styles.disabled : ''} ${className || ''}`}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </Button>
  );
}
