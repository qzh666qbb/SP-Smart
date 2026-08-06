package com.example.training.hour.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.training.hour.domain.model.HourPackage;
import com.example.training.hour.domain.model.StudentPackageRecord;
import com.example.training.hour.domain.repository.HourPackageRepository;
import com.example.training.hour.infrastructure.persistence.HourPackageEntity;
import com.example.training.hour.infrastructure.persistence.HourPackageMapper;
import com.example.training.hour.infrastructure.persistence.StudentPackageRecordEntity;
import com.example.training.hour.infrastructure.persistence.StudentPackageRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class HourPackageRepositoryImpl implements HourPackageRepository {

    private final HourPackageMapper hourPackageMapper;
    private final StudentPackageRecordMapper studentPackageRecordMapper;

    @Override
    public HourPackage savePackage(HourPackage hourPackage) {
        HourPackageEntity entity = convertToEntity(hourPackage);
        if (hourPackage.getId() == null) {
            hourPackageMapper.insert(entity);
        } else {
            hourPackageMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }

    @Override
    public Optional<HourPackage> findPackageById(Long id) {
        HourPackageEntity entity = hourPackageMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }

    @Override
    public List<HourPackage> listAllPackages() {
        List<HourPackageEntity> entities = hourPackageMapper.selectList(
                new LambdaQueryWrapper<HourPackageEntity>()
                        .isNull(HourPackageEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }

    @Override
    public StudentPackageRecord savePurchaseRecord(StudentPackageRecord record) {
        StudentPackageRecordEntity entity = convertToEntity(record);
        if (record.getId() == null) {
            studentPackageRecordMapper.insert(entity);
        } else {
            studentPackageRecordMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }

    @Override
    public List<StudentPackageRecord> findRecordsByStudentId(Long studentId) {
        List<StudentPackageRecordEntity> entities = studentPackageRecordMapper.selectList(
                new LambdaQueryWrapper<StudentPackageRecordEntity>()
                        .eq(StudentPackageRecordEntity::getStudentId, studentId)
                        .isNull(StudentPackageRecordEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }

    private HourPackageEntity convertToEntity(HourPackage domain) {
        HourPackageEntity entity = new HourPackageEntity();
        entity.setId(domain.getId());
        entity.setPackageCode(domain.getPackageCode());
        entity.setPackageName(domain.getPackageName());
        entity.setPackageType(domain.getPackageType());
        entity.setTotalHours(domain.getTotalHours());
        entity.setPrice(domain.getPrice());
        entity.setValidMonths(domain.getValidMonths());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedBy(domain.getUpdatedBy());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    private HourPackage convertToDomain(HourPackageEntity entity) {
        return HourPackage.builder()
                .id(entity.getId())
                .packageCode(entity.getPackageCode())
                .packageName(entity.getPackageName())
                .packageType(entity.getPackageType())
                .totalHours(entity.getTotalHours())
                .price(entity.getPrice())
                .validMonths(entity.getValidMonths())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private StudentPackageRecordEntity convertToEntity(StudentPackageRecord domain) {
        StudentPackageRecordEntity entity = new StudentPackageRecordEntity();
        entity.setId(domain.getId());
        entity.setStudentId(domain.getStudentId());
        entity.setPackageId(domain.getPackageId());
        entity.setPurchaseDate(domain.getPurchaseDate());
        entity.setExpireDate(domain.getExpireDate());
        entity.setTotalHours(domain.getTotalHours());
        entity.setRemainingHours(domain.getRemainingHours());
        entity.setConsumedHours(domain.getConsumedHours());
        entity.setRefundedHours(domain.getRefundedHours());
        entity.setStatus(domain.getStatus());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    private StudentPackageRecord convertToDomain(StudentPackageRecordEntity entity) {
        return StudentPackageRecord.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .packageId(entity.getPackageId())
                .purchaseDate(entity.getPurchaseDate())
                .expireDate(entity.getExpireDate())
                .totalHours(entity.getTotalHours())
                .remainingHours(entity.getRemainingHours())
                .consumedHours(entity.getConsumedHours())
                .refundedHours(entity.getRefundedHours())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
