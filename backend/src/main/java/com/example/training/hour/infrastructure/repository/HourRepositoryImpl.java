package com.example.training.hour.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.hour.domain.model.StudentHourAccount;
import com.example.training.hour.domain.repository.HourRepository;
import com.example.training.hour.infrastructure.persistence.HourTransactionEntity;
import com.example.training.hour.infrastructure.persistence.HourTransactionMapper;
import com.example.training.hour.infrastructure.persistence.StudentHourAccountEntity;
import com.example.training.hour.infrastructure.persistence.StudentHourAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class HourRepositoryImpl implements HourRepository {
    
    private final StudentHourAccountMapper accountMapper;
    private final HourTransactionMapper transactionMapper;
    
    @Override
    public StudentHourAccount saveAccount(StudentHourAccount account) {
        StudentHourAccountEntity entity = convertToEntity(account);
        if (account.getId() == null) {
            accountMapper.insert(entity);
        } else {
            accountMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<StudentHourAccount> findAccountByStudentId(Long studentId) {
        StudentHourAccountEntity entity = accountMapper.selectOne(
                new LambdaQueryWrapper<StudentHourAccountEntity>()
                        .eq(StudentHourAccountEntity::getStudentId, studentId)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public StudentHourAccount getAccountForUpdate(Long studentId) {
        return null;
    }
    
    @Override
    public HourTransaction saveTransaction(HourTransaction transaction) {
        HourTransactionEntity entity = convertToEntity(transaction);
        if (transaction.getId() == null) {
            transactionMapper.insert(entity);
        } else {
            transactionMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<HourTransaction> findTransactionByNo(String transactionNo) {
        HourTransactionEntity entity = transactionMapper.selectOne(
                new LambdaQueryWrapper<HourTransactionEntity>()
                        .eq(HourTransactionEntity::getTransactionNo, transactionNo)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<HourTransaction> findTransactionByKey(String idempotencyKey) {
        HourTransactionEntity entity = transactionMapper.selectOne(
                new LambdaQueryWrapper<HourTransactionEntity>()
                        .eq(HourTransactionEntity::getIdempotencyKey, idempotencyKey)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<HourTransaction> findTransactionsByStudentId(Long studentId) {
        List<HourTransactionEntity> entities = transactionMapper.selectList(
                new LambdaQueryWrapper<HourTransactionEntity>()
                        .eq(HourTransactionEntity::getStudentId, studentId)
                        .orderByDesc(HourTransactionEntity::getCreatedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<HourTransaction> findTransactionsByType(String transactionType) {
        List<HourTransactionEntity> entities = transactionMapper.selectList(
                new LambdaQueryWrapper<HourTransactionEntity>()
                        .eq(HourTransactionEntity::getTransactionType, transactionType)
                        .orderByDesc(HourTransactionEntity::getCreatedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    private StudentHourAccountEntity convertToEntity(StudentHourAccount account) {
        return StudentHourAccountEntity.builder()
                .id(account.getId())
                .studentId(account.getStudentId())
                .totalRecharged(account.getTotalRecharged())
                .totalConsumed(account.getTotalConsumed())
                .totalRefunded(account.getTotalRefunded())
                .remainingHours(account.getRemainingHours())
                .version(account.getVersion())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
    
    private StudentHourAccount convertToDomain(StudentHourAccountEntity entity) {
        return StudentHourAccount.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .totalRecharged(entity.getTotalRecharged())
                .totalConsumed(entity.getTotalConsumed())
                .totalRefunded(entity.getTotalRefunded())
                .remainingHours(entity.getRemainingHours())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    private HourTransactionEntity convertToEntity(HourTransaction transaction) {
        return HourTransactionEntity.builder()
                .id(transaction.getId())
                .transactionNo(transaction.getTransactionNo())
                .studentId(transaction.getStudentId())
                .transactionType(transaction.getTransactionType())
                .changeAmount(transaction.getChangeAmount())
                .beforeBalance(transaction.getBeforeBalance())
                .afterBalance(transaction.getAfterBalance())
                .lessonId(transaction.getLessonId())
                .lessonStudentId(transaction.getLessonStudentId())
                .relatedTransactionId(transaction.getRelatedTransactionId())
                .reason(transaction.getReason())
                .operatorId(transaction.getOperatorId())
                .operatorRole(transaction.getOperatorRole())
                .idempotencyKey(transaction.getIdempotencyKey())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
    
    private HourTransaction convertToDomain(HourTransactionEntity entity) {
        return HourTransaction.builder()
                .id(entity.getId())
                .transactionNo(entity.getTransactionNo())
                .studentId(entity.getStudentId())
                .transactionType(entity.getTransactionType())
                .changeAmount(entity.getChangeAmount())
                .beforeBalance(entity.getBeforeBalance())
                .afterBalance(entity.getAfterBalance())
                .lessonId(entity.getLessonId())
                .lessonStudentId(entity.getLessonStudentId())
                .relatedTransactionId(entity.getRelatedTransactionId())
                .reason(entity.getReason())
                .operatorId(entity.getOperatorId())
                .operatorRole(entity.getOperatorRole())
                .idempotencyKey(entity.getIdempotencyKey())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
