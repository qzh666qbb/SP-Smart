package com.example.training.physical.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import com.example.training.physical.infrastructure.persistence.PhysicalScoreDetailEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalScoreDetailMapper;
import com.example.training.physical.infrastructure.persistence.PhysicalTestItemEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalTestItemMapper;
import com.example.training.physical.infrastructure.persistence.PhysicalTestRecordEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalTestRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PhysicalTestRepositoryImpl implements PhysicalTestRepository {
    
    private final PhysicalTestItemMapper itemMapper;
    private final PhysicalTestRecordMapper recordMapper;
    private final PhysicalScoreDetailMapper detailMapper;
    
    @Override
    public PhysicalTestItem saveItem(PhysicalTestItem item) {
        PhysicalTestItemEntity entity = convertToEntity(item);
        if (item.getId() == null) {
            itemMapper.insert(entity);
        } else {
            itemMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<PhysicalTestItem> findItemById(Long id) {
        PhysicalTestItemEntity entity = itemMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<PhysicalTestItem> findAllEnabledItems() {
        List<PhysicalTestItemEntity> entities = itemMapper.selectList(
                new LambdaQueryWrapper<PhysicalTestItemEntity>()
                        .eq(PhysicalTestItemEntity::getStatus, "ENABLED")
                        .isNull(PhysicalTestItemEntity::getDeletedAt)
                        .orderByAsc(PhysicalTestItemEntity::getSortOrder)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public PhysicalTestRecord saveRecord(PhysicalTestRecord record) {
        PhysicalTestRecordEntity entity = convertToEntity(record);
        if (record.getId() == null) {
            recordMapper.insert(entity);
        } else {
            recordMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<PhysicalTestRecord> findRecordById(Long id) {
        PhysicalTestRecordEntity entity = recordMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<PhysicalTestRecord> findRecordByNo(String recordNo) {
        PhysicalTestRecordEntity entity = recordMapper.selectOne(
                new LambdaQueryWrapper<PhysicalTestRecordEntity>()
                        .eq(PhysicalTestRecordEntity::getRecordNo, recordNo)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<PhysicalTestRecord> findByStudentId(Long studentId) {
        List<PhysicalTestRecordEntity> entities = recordMapper.selectList(
                new LambdaQueryWrapper<PhysicalTestRecordEntity>()
                        .eq(PhysicalTestRecordEntity::getStudentId, studentId)
                        .orderByDesc(PhysicalTestRecordEntity::getTestDate)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<PhysicalTestRecord> findByBatchId(Long batchId) {
        List<PhysicalTestRecordEntity> entities = recordMapper.selectList(
                new LambdaQueryWrapper<PhysicalTestRecordEntity>()
                        .eq(PhysicalTestRecordEntity::getBatchId, batchId)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public PhysicalScoreDetail saveScoreDetail(PhysicalScoreDetail detail) {
        PhysicalScoreDetailEntity entity = convertToEntity(detail);
        if (detail.getId() == null) {
            detailMapper.insert(entity);
        } else {
            detailMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public List<PhysicalScoreDetail> findByRecordId(Long recordId) {
        List<PhysicalScoreDetailEntity> entities = detailMapper.selectList(
                new LambdaQueryWrapper<PhysicalScoreDetailEntity>()
                        .eq(PhysicalScoreDetailEntity::getRecordId, recordId)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public void deleteScoreDetails(Long recordId) {
        detailMapper.delete(
                new LambdaQueryWrapper<PhysicalScoreDetailEntity>()
                        .eq(PhysicalScoreDetailEntity::getRecordId, recordId)
        );
    }
    
    private PhysicalTestItemEntity convertToEntity(PhysicalTestItem item) {
        return PhysicalTestItemEntity.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemType(item.getItemType())
                .unit(item.getUnit())
                .genderSpecific(item.getGenderSpecific())
                .ageMin(item.getAgeMin())
                .ageMax(item.getAgeMax())
                .description(item.getDescription())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .createdBy(item.getCreatedBy())
                .createdAt(item.getCreatedAt())
                .updatedBy(item.getUpdatedBy())
                .updatedAt(item.getUpdatedAt())
                .deletedAt(item.getDeletedAt())
                .build();
    }
    
    private PhysicalTestItem convertToDomain(PhysicalTestItemEntity entity) {
        return PhysicalTestItem.builder()
                .id(entity.getId())
                .itemCode(entity.getItemCode())
                .itemName(entity.getItemName())
                .itemType(entity.getItemType())
                .unit(entity.getUnit())
                .genderSpecific(entity.getGenderSpecific())
                .ageMin(entity.getAgeMin())
                .ageMax(entity.getAgeMax())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .sortOrder(entity.getSortOrder())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    private PhysicalTestRecordEntity convertToEntity(PhysicalTestRecord record) {
        return PhysicalTestRecordEntity.builder()
                .id(record.getId())
                .recordNo(record.getRecordNo())
                .batchId(record.getBatchId())
                .studentId(record.getStudentId())
                .testDate(record.getTestDate())
                .testerId(record.getTesterId())
                .totalScore(record.getTotalScore())
                .overallLevel(record.getOverallLevel())
                .status(record.getStatus())
                .remark(record.getRemark())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
    
    private PhysicalTestRecord convertToDomain(PhysicalTestRecordEntity entity) {
        return PhysicalTestRecord.builder()
                .id(entity.getId())
                .recordNo(entity.getRecordNo())
                .batchId(entity.getBatchId())
                .studentId(entity.getStudentId())
                .testDate(entity.getTestDate())
                .testerId(entity.getTesterId())
                .totalScore(entity.getTotalScore())
                .overallLevel(entity.getOverallLevel())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    private PhysicalScoreDetailEntity convertToEntity(PhysicalScoreDetail detail) {
        return PhysicalScoreDetailEntity.builder()
                .id(detail.getId())
                .recordId(detail.getRecordId())
                .itemId(detail.getItemId())
                .rawValue(detail.getRawValue())
                .convertedScore(detail.getConvertedScore())
                .level(detail.getLevel())
                .remark(detail.getRemark())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }
    
    private PhysicalScoreDetail convertToDomain(PhysicalScoreDetailEntity entity) {
        return PhysicalScoreDetail.builder()
                .id(entity.getId())
                .recordId(entity.getRecordId())
                .itemId(entity.getItemId())
                .rawValue(entity.getRawValue())
                .convertedScore(entity.getConvertedScore())
                .level(entity.getLevel())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    @Override
    public List<PhysicalTestRecord> findByTesterId(Long testerId) {
        List<PhysicalTestRecordEntity> entities = recordMapper.selectList(
                new LambdaQueryWrapper<PhysicalTestRecordEntity>()
                        .eq(PhysicalTestRecordEntity::getTesterId, testerId)
                        .orderByDesc(PhysicalTestRecordEntity::getTestDate)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public Optional<PhysicalTestRecord> findLatestByStudentId(Long studentId) {
        PhysicalTestRecordEntity entity = recordMapper.selectOne(
                new LambdaQueryWrapper<PhysicalTestRecordEntity>()
                        .eq(PhysicalTestRecordEntity::getStudentId, studentId)
                        .orderByDesc(PhysicalTestRecordEntity::getTestDate)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
}
