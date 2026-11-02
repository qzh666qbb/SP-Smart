package com.example.training.physical.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalScoreStandard;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestBatch;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import com.example.training.physical.infrastructure.persistence.PhysicalScoreDetailEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalScoreDetailMapper;
import com.example.training.physical.infrastructure.persistence.PhysicalScoreStandardEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalScoreStandardMapper;
import com.example.training.physical.infrastructure.persistence.PhysicalTestItemEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalTestItemMapper;
import com.example.training.physical.infrastructure.persistence.PhysicalTestBatchEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalTestBatchMapper;
import com.example.training.physical.infrastructure.persistence.PhysicalTestRecordEntity;
import com.example.training.physical.infrastructure.persistence.PhysicalTestRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PhysicalTestRepositoryImpl implements PhysicalTestRepository {

    private final PhysicalTestRecordMapper recordMapper;
    private final PhysicalScoreDetailMapper detailMapper;
    private final PhysicalScoreStandardMapper standardMapper;
    private final PhysicalTestItemMapper physicalTestItemMapper;
    private final PhysicalTestBatchMapper batchMapper;

    @Override
    public PhysicalTestBatch saveBatch(PhysicalTestBatch batch) {
        PhysicalTestBatchEntity entity = new PhysicalTestBatchEntity();
        entity.setId(batch.getId());
        entity.setBatchName(batch.getBatchName());
        entity.setBatchCode(batch.getBatchCode());
        entity.setTemplateId(batch.getTemplateId());
        entity.setTestDate(batch.getTestDate());
        entity.setLocation(batch.getLocation());
        entity.setStatus(batch.getStatus());
        entity.setCreatedBy(batch.getCreatedBy());
        entity.setUpdatedBy(batch.getCreatedBy());
        if (entity.getId() == null) batchMapper.insert(entity); else batchMapper.updateById(entity);
        batch.setId(entity.getId());
        batch.setCreatedAt(entity.getCreatedAt());
        return batch;
    }

    @Override
    public Optional<PhysicalTestBatch> findBatchById(Long id) {
        return Optional.ofNullable(batchMapper.selectOne(new QueryWrapper<PhysicalTestBatchEntity>()
                        .eq("id", id).isNull("deleted_at")))
                .map(this::toBatchModel);
    }

    @Override
    public List<PhysicalTestBatch> findAvailableBatches() {
        return batchMapper.selectList(new QueryWrapper<PhysicalTestBatchEntity>()
                        .in("status", "PENDING", "IN_PROGRESS")
                        .isNull("deleted_at")
                        .orderByDesc("test_date").orderByDesc("id"))
                .stream().map(this::toBatchModel).toList();
    }

    @Override
    public Long findDefaultTemplateId() {
        return batchMapper.findDefaultTemplateId();
    }

    private PhysicalTestBatch toBatchModel(PhysicalTestBatchEntity entity) {
        return PhysicalTestBatch.builder().id(entity.getId()).batchName(entity.getBatchName())
                .batchCode(entity.getBatchCode()).templateId(entity.getTemplateId())
                .testDate(entity.getTestDate()).location(entity.getLocation()).status(entity.getStatus())
                .createdBy(entity.getCreatedBy()).createdAt(entity.getCreatedAt()).build();
    }

    @Override
    public PhysicalScoreStandard saveScoreStandard(PhysicalScoreStandard standard) {
        PhysicalScoreStandardEntity entity = toStandardEntity(standard);
        if (entity.getId() == null) {
            standardMapper.insert(entity);
        } else {
            standardMapper.updateById(entity);
        }
        return toStandardModel(entity);
    }

    @Override
    public List<PhysicalScoreStandard> findStandardsByItemIdAndGender(Long itemId, String gender, String ageGroup) {
        List<PhysicalScoreStandardEntity> entities = standardMapper.findByItemIdAndGenderAndAgeGroup(itemId, gender, ageGroup);
        return entities.stream().map(this::toStandardModel).toList();
    }

    @Override
    public void deleteScoreStandardsByItemId(Long itemId) {
        standardMapper.delete(new QueryWrapper<PhysicalScoreStandardEntity>()
                .eq("item_id", itemId));
    }

    @Override
    public List<PhysicalScoreStandard> findAllStandards() {
        return standardMapper.selectList(null).stream().map(this::toStandardModel).toList();
    }

    @Override
    public PhysicalTestRecord saveRecord(PhysicalTestRecord record) {
        PhysicalTestRecordEntity entity = toRecordEntity(record);
        if (entity.getId() == null) {
            recordMapper.insert(entity);
        } else {
            recordMapper.updateById(entity);
        }
        return toRecordModel(entity);
    }

    @Override
    public Optional<PhysicalTestRecord> findRecordById(Long id) {
        return Optional.ofNullable(recordMapper.selectOne(
                new QueryWrapper<PhysicalTestRecordEntity>()
                        .eq("id", id)
                        .isNull("deleted_at")))
                .map(this::toRecordModel);
    }

    @Override
    public Optional<PhysicalTestRecord> findRecordByNo(String recordNo) {
        return Optional.ofNullable(recordMapper.selectOne(
                new QueryWrapper<PhysicalTestRecordEntity>()
                        .eq("record_no", recordNo)
                        .isNull("deleted_at")))
                .map(this::toRecordModel);
    }

    @Override
    public List<PhysicalTestRecord> findByStudentId(Long studentId) {
        return recordMapper.selectList(
                new QueryWrapper<PhysicalTestRecordEntity>()
                        .eq("student_id", studentId)
                        .isNull("deleted_at")
                        .orderByDesc("test_date")
                        .orderByDesc("id"))
                .stream().map(this::toRecordModel).toList();
    }

    @Override
    public List<PhysicalTestRecord> findByBatchId(Long batchId) {
        return recordMapper.selectList(
                new QueryWrapper<PhysicalTestRecordEntity>()
                        .eq("batch_id", batchId)
                        .isNull("deleted_at"))
                .stream().map(this::toRecordModel).toList();
    }

    @Override
    public PhysicalScoreDetail saveScoreDetail(PhysicalScoreDetail detail) {
        PhysicalScoreDetailEntity entity = toDetailEntity(detail);
        if (entity.getId() == null) {
            detailMapper.insert(entity);
        } else {
            detailMapper.updateById(entity);
        }
        return toDetailModel(entity);
    }

    @Override
    public List<PhysicalScoreDetail> findByRecordId(Long recordId) {
        return detailMapper.selectList(
                new QueryWrapper<PhysicalScoreDetailEntity>()
                        .eq("record_id", recordId))
                .stream().map(this::toDetailModel).toList();
    }

    @Override
    public void deleteScoreDetails(Long recordId) {
        detailMapper.delete(new QueryWrapper<PhysicalScoreDetailEntity>()
                .eq("record_id", recordId));
    }

    @Override
    public List<PhysicalTestRecord> findByTesterId(Long testerId) {
        QueryWrapper<PhysicalTestRecordEntity> wrapper = new QueryWrapper<>();
        if (testerId != null) wrapper.eq("tester_id", testerId);
        wrapper.isNull("deleted_at")
                .orderByDesc("test_date")
                .orderByDesc("id");
        return recordMapper.selectList(wrapper)
                .stream().map(this::toRecordModel).toList();
    }

    @Override
    public Optional<PhysicalTestRecord> findLatestByStudentId(Long studentId) {
        return Optional.ofNullable(recordMapper.selectOne(
                new QueryWrapper<PhysicalTestRecordEntity>()
                        .eq("student_id", studentId)
                        .isNull("deleted_at")
                        .orderByDesc("test_date")
                        .last("LIMIT 1")))
                .map(this::toRecordModel);
    }

    @Override
    public PhysicalTestItem saveItem(PhysicalTestItem item) {
        PhysicalTestItemEntity entity = toItemEntity(item);
        if (entity.getId() == null) {
            physicalTestItemMapper.insert(entity);
        } else {
            physicalTestItemMapper.updateById(entity);
        }
        return toItemModel(entity);
    }

    @Override
    public Optional<PhysicalTestItem> findItemById(Long id) {
        return Optional.ofNullable(physicalTestItemMapper.selectById(id)).map(this::toItemModel);
    }

    @Override
    public List<PhysicalTestItem> findAllEnabledItems() {
        return physicalTestItemMapper.selectList(
                new QueryWrapper<PhysicalTestItemEntity>()
                        .eq("status", "ENABLED")
                        .orderByAsc("sort_order"))
                .stream().map(this::toItemModel).toList();
    }

    private PhysicalScoreStandardEntity toStandardEntity(PhysicalScoreStandard model) {
        PhysicalScoreStandardEntity e = new PhysicalScoreStandardEntity();
        e.setId(model.getId());
        e.setItemId(model.getItemId());
        e.setGender(model.getGender());
        e.setAgeGroup(model.getAgeGroup());
        e.setMinValue(model.getMinValue());
        e.setMaxValue(model.getMaxValue());
        e.setScore(model.getScore());
        e.setLevel(model.getLevel());
        e.setScoreValue(model.getScoreValue());
        e.setCreatedBy(model.getCreatedBy());
        e.setCreatedAt(model.getCreatedAt());
        e.setUpdatedAt(model.getUpdatedAt());
        return e;
    }

    private PhysicalScoreStandard toStandardModel(PhysicalScoreStandardEntity e) {
        return PhysicalScoreStandard.builder()
                .id(e.getId())
                .itemId(e.getItemId())
                .gender(e.getGender())
                .ageGroup(e.getAgeGroup())
                .minValue(e.getMinValue())
                .maxValue(e.getMaxValue())
                .score(e.getScore())
                .level(e.getLevel())
                .scoreValue(e.getScoreValue())
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private PhysicalTestRecordEntity toRecordEntity(PhysicalTestRecord model) {
        PhysicalTestRecordEntity e = new PhysicalTestRecordEntity();
        e.setId(model.getId());
        e.setRecordNo(model.getRecordNo());
        e.setBatchId(model.getBatchId());
        e.setStudentId(model.getStudentId());
        e.setTestDate(model.getTestDate());
        e.setTesterId(model.getTesterId());
        e.setTotalScore(model.getTotalScore());
        e.setOverallLevel(model.getOverallLevel());
        e.setStatus(model.getStatus());
        e.setRemark(model.getRemark());
        e.setCreatedAt(model.getCreatedAt());
        e.setUpdatedAt(model.getUpdatedAt());
        e.setDeletedAt(model.getDeletedAt());
        return e;
    }

    private PhysicalTestRecord toRecordModel(PhysicalTestRecordEntity e) {
        return PhysicalTestRecord.builder()
                .id(e.getId())
                .recordNo(e.getRecordNo())
                .batchId(e.getBatchId())
                .studentId(e.getStudentId())
                .testDate(e.getTestDate())
                .testerId(e.getTesterId())
                .totalScore(e.getTotalScore())
                .overallLevel(e.getOverallLevel())
                .status(e.getStatus())
                .remark(e.getRemark())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }

    private PhysicalScoreDetailEntity toDetailEntity(PhysicalScoreDetail model) {
        PhysicalScoreDetailEntity e = new PhysicalScoreDetailEntity();
        e.setId(model.getId());
        e.setRecordId(model.getRecordId());
        e.setItemId(model.getItemId());
        e.setRawValue(model.getRawValue());
        e.setConvertedScore(model.getConvertedScore());
        e.setLevel(model.getLevel());
        e.setRemark(model.getRemark());
        return e;
    }

    private PhysicalScoreDetail toDetailModel(PhysicalScoreDetailEntity e) {
        return PhysicalScoreDetail.builder()
                .id(e.getId())
                .recordId(e.getRecordId())
                .itemId(e.getItemId())
                .rawValue(e.getRawValue())
                .convertedScore(e.getConvertedScore())
                .level(e.getLevel())
                .remark(e.getRemark())
                .build();
    }

    private PhysicalTestItemEntity toItemEntity(PhysicalTestItem model) {
        return PhysicalTestItemEntity.builder()
                .id(model.getId())
                .itemCode(model.getItemCode())
                .itemName(model.getItemName())
                .itemType(model.getItemType())
                .unit(model.getUnit())
                .genderSpecific(model.getGenderSpecific())
                .ageMin(model.getAgeMin())
                .ageMax(model.getAgeMax())
                .description(model.getDescription())
                .status(model.getStatus())
                .sortOrder(model.getSortOrder())
                .createdBy(model.getCreatedBy())
                .createdAt(model.getCreatedAt())
                .updatedBy(model.getUpdatedBy())
                .updatedAt(model.getUpdatedAt())
                .deletedAt(model.getDeletedAt())
                .build();
    }

    private PhysicalTestItem toItemModel(PhysicalTestItemEntity e) {
        return PhysicalTestItem.builder()
                .id(e.getId())
                .itemCode(e.getItemCode())
                .itemName(e.getItemName())
                .itemType(e.getItemType())
                .unit(e.getUnit())
                .genderSpecific(e.getGenderSpecific())
                .ageMin(e.getAgeMin())
                .ageMax(e.getAgeMax())
                .description(e.getDescription())
                .status(e.getStatus())
                .sortOrder(e.getSortOrder())
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .updatedBy(e.getUpdatedBy())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
