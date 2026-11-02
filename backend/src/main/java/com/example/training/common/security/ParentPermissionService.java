package com.example.training.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.training.student.infrastructure.persistence.ParentStudentRelationEntity;
import com.example.training.student.infrastructure.persistence.ParentStudentRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentPermissionService {

    private final ParentStudentRelationMapper parentStudentRelationMapper;

    public boolean canAccessStudent(CurrentUser user, Long studentId) {
        if (user == null || studentId == null) {
            return false;
        }
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        if ("PARENT".equals(user.getRole())) {
            if (user.getProfileId() == null) return false;
            Long count = parentStudentRelationMapper.selectCount(
                    new LambdaQueryWrapper<ParentStudentRelationEntity>()
                            .eq(ParentStudentRelationEntity::getParentId, user.getProfileId())
                            .eq(ParentStudentRelationEntity::getStudentId, studentId)
                            .eq(ParentStudentRelationEntity::getBindStatus, "CONFIRMED")
            );
            return count != null && count > 0;
        }
        return false;
    }

    public Long resolveParentProfileId(CurrentUser user) {
        return user != null ? user.getProfileId() : null;
    }
}
