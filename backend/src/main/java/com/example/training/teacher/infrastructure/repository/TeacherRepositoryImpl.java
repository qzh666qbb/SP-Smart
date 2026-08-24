package com.example.training.teacher.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.training.teacher.domain.model.TeacherApply;
import com.example.training.teacher.domain.model.TeacherPreRegister;
import com.example.training.teacher.domain.repository.TeacherRepository;
import com.example.training.teacher.infrastructure.persistence.TeacherApplyEntity;
import com.example.training.teacher.infrastructure.persistence.TeacherApplyMapper;
import com.example.training.teacher.infrastructure.persistence.TeacherPreRegisterEntity;
import com.example.training.teacher.infrastructure.persistence.TeacherPreRegisterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TeacherRepositoryImpl implements TeacherRepository {

    private final TeacherApplyMapper teacherApplyMapper;
    private final TeacherPreRegisterMapper teacherPreRegisterMapper;

    @Override
    public TeacherApply saveApply(TeacherApply apply) {
        TeacherApplyEntity entity = convertApplyToEntity(apply);
        if (apply.getId() == null) {
            teacherApplyMapper.insert(entity);
        } else {
            teacherApplyMapper.updateById(entity);
        }
        return convertApplyToDomain(entity);
    }

    @Override
    public Optional<TeacherApply> findApplyById(Long id) {
        TeacherApplyEntity entity = teacherApplyMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertApplyToDomain);
    }

    @Override
    public Optional<TeacherApply> findApplyByPhone(String phone) {
        TeacherApplyEntity entity = teacherApplyMapper.selectOne(
                new LambdaQueryWrapper<TeacherApplyEntity>()
                        .eq(TeacherApplyEntity::getPhone, phone)
                        .orderByDesc(TeacherApplyEntity::getId)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::convertApplyToDomain);
    }

    @Override
    public Optional<TeacherApply> findApplyByOpenid(String openid) {
        if (openid == null || openid.isBlank()) return Optional.empty();
        TeacherApplyEntity entity = teacherApplyMapper.selectOne(
                new LambdaQueryWrapper<TeacherApplyEntity>()
                        .eq(TeacherApplyEntity::getWechatOpenid, openid)
                        .orderByDesc(TeacherApplyEntity::getId)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::convertApplyToDomain);
    }

    @Override
    public Optional<TeacherApply> findApplyByPhoneOrOpenid(String phone, String openid) {
        LambdaQueryWrapper<TeacherApplyEntity> q = new LambdaQueryWrapper<>();
        boolean hasCondition = false;
        if (phone != null && !phone.isBlank()) {
            q.eq(TeacherApplyEntity::getPhone, phone);
            hasCondition = true;
        }
        if (openid != null && !openid.isBlank()) {
            if (hasCondition) {
                q.or();
            }
            q.eq(TeacherApplyEntity::getWechatOpenid, openid);
            hasCondition = true;
        }
        if (!hasCondition) return Optional.empty();
        q.orderByDesc(TeacherApplyEntity::getId).last("LIMIT 1");
        TeacherApplyEntity entity = teacherApplyMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::convertApplyToDomain);
    }

    @Override
    public List<TeacherApply> findApplies(String keyword, String status, int pageNum, int pageSize) {
        Page<TeacherApplyEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TeacherApplyEntity> wrapper = buildApplyFilterWrapper(keyword, status);
        wrapper.orderByDesc(TeacherApplyEntity::getId);
        return teacherApplyMapper.selectPage(page, wrapper).getRecords().stream()
                .map(this::convertApplyToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countApplies(String keyword, String status) {
        return teacherApplyMapper.selectCount(buildApplyFilterWrapper(keyword, status));
    }

    private LambdaQueryWrapper<TeacherApplyEntity> buildApplyFilterWrapper(String keyword, String status) {
        LambdaQueryWrapper<TeacherApplyEntity> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            wrapper.and(w -> w.like(TeacherApplyEntity::getPhone, kw)
                    .or().like(TeacherApplyEntity::getRealName, kw));
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(TeacherApplyEntity::getStatus, status.trim());
        }
        return wrapper;
    }

    @Override
    public TeacherPreRegister savePreRegister(TeacherPreRegister preRegister) {
        TeacherPreRegisterEntity entity = convertPreRegisterToEntity(preRegister);
        if (preRegister.getId() == null) {
            teacherPreRegisterMapper.insert(entity);
        } else {
            teacherPreRegisterMapper.updateById(entity);
        }
        return convertPreRegisterToDomain(entity);
    }

    @Override
    public Optional<TeacherPreRegister> findPreRegisterByPhone(String phone) {
        TeacherPreRegisterEntity entity = teacherPreRegisterMapper.selectOne(
                new LambdaQueryWrapper<TeacherPreRegisterEntity>()
                        .eq(TeacherPreRegisterEntity::getPhone, phone)
                        .eq(TeacherPreRegisterEntity::getStatus, "ACTIVE")
                        .orderByDesc(TeacherPreRegisterEntity::getId)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::convertPreRegisterToDomain);
    }

    @Override
    public List<TeacherPreRegister> findPreRegisters(String keyword, int pageNum, int pageSize) {
        Page<TeacherPreRegisterEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TeacherPreRegisterEntity> wrapper = buildPreRegisterFilterWrapper(keyword);
        wrapper.orderByDesc(TeacherPreRegisterEntity::getId);
        return teacherPreRegisterMapper.selectPage(page, wrapper).getRecords().stream()
                .map(this::convertPreRegisterToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countPreRegisters(String keyword) {
        return teacherPreRegisterMapper.selectCount(buildPreRegisterFilterWrapper(keyword));
    }

    private LambdaQueryWrapper<TeacherPreRegisterEntity> buildPreRegisterFilterWrapper(String keyword) {
        LambdaQueryWrapper<TeacherPreRegisterEntity> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            wrapper.and(w -> w.like(TeacherPreRegisterEntity::getPhone, kw)
                    .or().like(TeacherPreRegisterEntity::getRealName, kw));
        }
        return wrapper;
    }

    private TeacherApplyEntity convertApplyToEntity(TeacherApply apply) {
        return TeacherApplyEntity.builder()
                .id(apply.getId())
                .phone(apply.getPhone())
                .realName(apply.getRealName())
                .wechatOpenid(apply.getWechatOpenid())
                .status(apply.getStatus())
                .reviewerId(apply.getReviewerId())
                .reviewComment(apply.getReviewComment())
                .reviewedAt(apply.getReviewedAt())
                .createdBy(apply.getCreatedBy())
                .createdAt(apply.getCreatedAt())
                .updatedAt(apply.getUpdatedAt())
                .build();
    }

    private TeacherApply convertApplyToDomain(TeacherApplyEntity entity) {
        return TeacherApply.builder()
                .id(entity.getId())
                .phone(entity.getPhone())
                .realName(entity.getRealName())
                .wechatOpenid(entity.getWechatOpenid())
                .status(entity.getStatus())
                .reviewerId(entity.getReviewerId())
                .reviewComment(entity.getReviewComment())
                .reviewedAt(entity.getReviewedAt())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private TeacherPreRegisterEntity convertPreRegisterToEntity(TeacherPreRegister preRegister) {
        return TeacherPreRegisterEntity.builder()
                .id(preRegister.getId())
                .phone(preRegister.getPhone())
                .realName(preRegister.getRealName())
                .createdBy(preRegister.getCreatedBy())
                .status(preRegister.getStatus())
                .createdAt(preRegister.getCreatedAt())
                .updatedAt(preRegister.getUpdatedAt())
                .build();
    }

    private TeacherPreRegister convertPreRegisterToDomain(TeacherPreRegisterEntity entity) {
        return TeacherPreRegister.builder()
                .id(entity.getId())
                .phone(entity.getPhone())
                .realName(entity.getRealName())
                .createdBy(entity.getCreatedBy())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
