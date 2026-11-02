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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class HourRepositoryImpl implements HourRepository {
    
    private final StudentHourAccountMapper accountMapper;
    private final HourTransactionMapper transactionMapper;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public StudentHourAccount saveAccount(StudentHourAccount account) {
        StudentHourAccountEntity entity = convertToEntity(account);
        if (account.getId() == null) {
            accountMapper.insert(entity);
            if (entity.getId() == null && account.getStudentId() != null) {
                StudentHourAccountEntity found = accountMapper.selectOne(
                    new LambdaQueryWrapper<StudentHourAccountEntity>()
                        .eq(StudentHourAccountEntity::getStudentId, account.getStudentId())
                        .orderByDesc(StudentHourAccountEntity::getId)
                        .last("LIMIT 1"));
                if (found != null) {
                    entity.setId(found.getId());
                }
            }
        } else {
            int rows = accountMapper.updateWithVersion(entity);
            if (rows == 0) {
                throw new com.baomidou.mybatisplus.core.exceptions.MybatisPlusException("课时账户版本冲突，数据已被并发修改: id=" + account.getId());
            }
        }
        StudentHourAccountEntity saved = accountMapper.selectById(entity.getId());
        return convertToDomain(saved);
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
        StudentHourAccountEntity entity = accountMapper.selectForUpdate(studentId);
        if (entity == null) {
            return null;
        }
        return convertToDomain(entity);
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
    public Optional<HourTransaction> findTransactionById(Long id) {
        HourTransactionEntity entity = transactionMapper.selectById(id);
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

    @Override
    public List<Object[]> listAccountsRaw(String keyword, int offset, int pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.student_id, s.name AS student_name, s.student_no, " +
                        "s.parent_name, s.parent_phone, " +
                        "COALESCE(a.total_recharged, 0), COALESCE(a.total_consumed, 0), COALESCE(a.remaining_hours, 0), " +
                        "COALESCE(a.paid_hours, 0), COALESCE(a.gift_hours, 0), a.created_at " +
                        "FROM student s LEFT JOIN student_hour_account a ON a.student_id = s.id " +
                        "WHERE s.deleted_at IS NULL ");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (s.name LIKE ? OR s.student_no LIKE ? OR s.parent_name LIKE ? OR s.parent_phone LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like); args.add(like); args.add(like); args.add(like);
        }
        sql.append("ORDER BY a.updated_at DESC, s.id DESC LIMIT ? OFFSET ?");
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql.toString(), args.toArray(), (rs, row) -> new Object[]{
                rs.getObject(1, Long.class),
                rs.getObject(2, Long.class),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getBigDecimal(7),
                rs.getBigDecimal(8),
                rs.getBigDecimal(9),
                rs.getBigDecimal(10),
                rs.getBigDecimal(11),
                rs.getTimestamp(12) != null ? rs.getTimestamp(12).toLocalDateTime() : null
        });
    }

    @Override
    public long countAccounts(String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM student s LEFT JOIN student_hour_account a ON a.student_id = s.id " +
                        "WHERE s.deleted_at IS NULL ");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (s.name LIKE ? OR s.student_no LIKE ? OR s.parent_name LIKE ? OR s.parent_phone LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            args.add(like); args.add(like); args.add(like); args.add(like);
        }
        Long cnt = jdbcTemplate.queryForObject(sql.toString(), args.toArray(), Long.class);
        return cnt == null ? 0L : cnt;
    }
    
    private StudentHourAccountEntity convertToEntity(StudentHourAccount account) {
        return StudentHourAccountEntity.builder()
                .id(account.getId())
                .studentId(account.getStudentId())
                .totalRecharged(account.getTotalRecharged())
                .totalConsumed(account.getTotalConsumed())
                .totalRefunded(account.getTotalRefunded())
                .remainingHours(account.getRemainingHours())
                .paidHours(account.getPaidHours())
                .giftHours(account.getGiftHours())
                .experienceHours(account.getExperienceHours())
                .activityHours(account.getActivityHours())
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
                .paidHours(entity.getPaidHours())
                .giftHours(entity.getGiftHours())
                .experienceHours(entity.getExperienceHours())
                .activityHours(entity.getActivityHours())
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
                .hourSource(transaction.getHourSource())
                .sourceBreakdown(transaction.getSourceBreakdown())
                .packageRecordId(transaction.getPackageRecordId())
                .expireDate(transaction.getExpireDate())
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
                .hourSource(entity.getHourSource())
                .sourceBreakdown(entity.getSourceBreakdown())
                .packageRecordId(entity.getPackageRecordId())
                .expireDate(entity.getExpireDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
