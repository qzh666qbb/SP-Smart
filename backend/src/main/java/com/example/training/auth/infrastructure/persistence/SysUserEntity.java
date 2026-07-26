package com.example.training.auth.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUserEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String passwordHash;
    private String role;
    private String phone;
    private String wechatOpenid;
    private String wechatUnionid;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordUpdatedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
