package com.example.training.auth.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.auth.infrastructure.persistence.ParentProfileEntity;
import com.example.training.auth.infrastructure.persistence.ParentProfileMapper;
import com.example.training.auth.infrastructure.persistence.SysUserEntity;
import com.example.training.auth.infrastructure.persistence.SysUserMapper;
import com.example.training.auth.infrastructure.persistence.TeacherProfileEntity;
import com.example.training.auth.infrastructure.persistence.TeacherProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final SysUserMapper sysUserMapper;
    private final TeacherProfileMapper teacherProfileMapper;
    private final ParentProfileMapper parentProfileMapper;
    
    @Override
    public User save(User user) {
        SysUserEntity entity = convertToEntity(user);
        if (user.getId() == null) {
            sysUserMapper.insert(entity);
        } else {
            sysUserMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        SysUserEntity entity = sysUserMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        SysUserEntity entity = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getUsername, username)
                        .isNull(SysUserEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<User> findByPhone(String phone) {
        SysUserEntity entity = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getPhone, phone)
                        .isNull(SysUserEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<User> findByWechatOpenid(String openid) {
        SysUserEntity entity = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getWechatOpenid, openid)
                        .isNull(SysUserEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }

    @Override
    public Optional<User> findByWechatUnionid(String unionid) {
        if (unionid == null || unionid.isBlank()) return Optional.empty();
        SysUserEntity entity = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getWechatUnionid, unionid)
                        .isNull(SysUserEntity::getDeletedAt)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }

    @Override
    public List<User> findAll(int pageNum, int pageSize) {
        return findAll(null, null, pageNum, pageSize);
    }

    @Override
    public List<User> findAll(String keyword, String role, int pageNum, int pageSize) {
        Page<SysUserEntity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUserEntity> wrapper = buildUserFilterWrapper(keyword, role);
        wrapper.orderByDesc(SysUserEntity::getId);
        return sysUserMapper.selectPage(page, wrapper).getRecords().stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return countAll(null, null);
    }

    @Override
    public long countAll(String keyword, String role) {
        return sysUserMapper.selectCount(buildUserFilterWrapper(keyword, role));
    }

    @Override
    public void updateLastLoginAt(Long userId) {
        sysUserMapper.updateLastLoginAt(userId);
    }

    private LambdaQueryWrapper<SysUserEntity> buildUserFilterWrapper(String keyword, String role) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SysUserEntity::getDeletedAt);
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            wrapper.and(w -> w.like(SysUserEntity::getUsername, kw)
                    .or().like(SysUserEntity::getPhone, kw));
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(SysUserEntity::getRole, role.trim());
        }
        return wrapper;
    }
    
    @Override
    public TeacherProfile saveTeacherProfile(TeacherProfile profile) {
        TeacherProfileEntity entity = convertToEntity(profile);
        if (profile.getId() == null) {
            teacherProfileMapper.insert(entity);
        } else {
            teacherProfileMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<TeacherProfile> findTeacherProfileByUserId(Long userId) {
        TeacherProfileEntity entity = teacherProfileMapper.selectOne(
                new LambdaQueryWrapper<TeacherProfileEntity>()
                        .eq(TeacherProfileEntity::getUserId, userId)
                        .isNull(TeacherProfileEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }

    @Override
    public Optional<TeacherProfile> findTeacherProfileById(Long profileId) {
        TeacherProfileEntity entity = teacherProfileMapper.selectOne(
                new LambdaQueryWrapper<TeacherProfileEntity>()
                        .eq(TeacherProfileEntity::getId, profileId)
                        .isNull(TeacherProfileEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public ParentProfile saveParentProfile(ParentProfile profile) {
        ParentProfileEntity entity = convertToEntity(profile);
        if (profile.getId() == null) {
            parentProfileMapper.insert(entity);
        } else {
            parentProfileMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<ParentProfile> findParentProfileByUserId(Long userId) {
        ParentProfileEntity entity = parentProfileMapper.selectOne(
                new LambdaQueryWrapper<ParentProfileEntity>()
                        .eq(ParentProfileEntity::getUserId, userId)
                        .isNull(ParentProfileEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    private SysUserEntity convertToEntity(User user) {
        return SysUserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .wechatOpenid(user.getWechatOpenid())
                .wechatUnionid(user.getWechatUnionid())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .passwordUpdatedAt(user.getPasswordUpdatedAt())
                .tokenVersion(user.getTokenVersion())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
    
    private User convertToDomain(SysUserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .role(entity.getRole())
                .phone(entity.getPhone())
                .nickname(entity.getNickname())
                .avatarUrl(entity.getAvatarUrl())
                .wechatOpenid(entity.getWechatOpenid())
                .wechatUnionid(entity.getWechatUnionid())
                .status(entity.getStatus())
                .lastLoginAt(entity.getLastLoginAt())
                .passwordUpdatedAt(entity.getPasswordUpdatedAt())
                .tokenVersion(entity.getTokenVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    private TeacherProfileEntity convertToEntity(TeacherProfile profile) {
        return TeacherProfileEntity.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .teacherNo(profile.getTeacherNo())
                .name(profile.getName())
                .gender(profile.getGender())
                .avatarUrl(profile.getAvatarUrl())
                .introduction(profile.getIntroduction())
                .employmentStatus(profile.getEmploymentStatus())
                .joinedDate(profile.getJoinedDate())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .deletedAt(profile.getDeletedAt())
                .build();
    }
    
    private TeacherProfile convertToDomain(TeacherProfileEntity entity) {
        return TeacherProfile.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .teacherNo(entity.getTeacherNo())
                .name(entity.getName())
                .gender(entity.getGender())
                .avatarUrl(entity.getAvatarUrl())
                .introduction(entity.getIntroduction())
                .employmentStatus(entity.getEmploymentStatus())
                .joinedDate(entity.getJoinedDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    private ParentProfileEntity convertToEntity(ParentProfile profile) {
        return ParentProfileEntity.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .name(profile.getName())
                .phone(profile.getPhone())
                .relationRemark(profile.getRelationRemark())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .deletedAt(profile.getDeletedAt())
                .build();
    }
    
    private ParentProfile convertToDomain(ParentProfileEntity entity) {
        return ParentProfile.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .relationRemark(entity.getRelationRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
