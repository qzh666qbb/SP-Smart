package com.example.training.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.training.common.security.CurrentUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long uid = currentUserIdOrDefault();
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createdBy", Long.class, uid);
        strictInsertFill(metaObject, "updatedBy", Long.class, uid);
        strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long uid = currentUserIdOrDefault();
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);
        strictUpdateFill(metaObject, "updatedBy", Long.class, uid);
    }

    private Long currentUserIdOrDefault() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CurrentUser) {
                Long id = ((CurrentUser) auth.getPrincipal()).getUserId();
                if (id != null) return id;
            }
        } catch (Exception ignore) {
        }
        return 0L;
    }
}
