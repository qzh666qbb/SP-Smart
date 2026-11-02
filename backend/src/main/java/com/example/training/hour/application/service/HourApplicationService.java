package com.example.training.hour.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.dto.HourAccountResponse;
import com.example.training.hour.application.dto.HourRechargeRequest;
import com.example.training.hour.application.dto.HourRefundRequest;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.hour.domain.model.StudentHourAccount;
import com.example.training.hour.domain.repository.HourRepository;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HourApplicationService {
    private static final Logger log = LoggerFactory.getLogger(HourApplicationService.class);

    
    private final HourRepository hourRepository;
    
    @Value("${app.hour.allow-negative-balance:false}")
    private boolean allowNegativeBalance;
    
    private BigDecimal safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
    
    private BigDecimal calcRemaining(StudentHourAccount a) {
        return safe(a.getPaidHours())
                .add(safe(a.getGiftHours()))
                .add(safe(a.getExperienceHours()))
                .add(safe(a.getActivityHours()));
    }
    
    @Transactional
    public StudentHourAccount recharge(HourRechargeRequest request, CurrentUser currentUser) {
        return recharge(request, currentUser, null);
    }

    @Transactional
    public StudentHourAccount recharge(HourRechargeRequest request, CurrentUser currentUser, LocalDate expireDate) {
        return addHours(request, currentUser, expireDate, "RECHARGE");
    }

    @Transactional
    public StudentHourAccount adjust(HourRechargeRequest request, CurrentUser currentUser) {
        return addHours(request, currentUser, null, "ADJUST");
    }

    private StudentHourAccount addHours(HourRechargeRequest request, CurrentUser currentUser,
                                        LocalDate expireDate, String transactionType) {
        StudentHourAccount account = hourRepository.findAccountByStudentId(request.getStudentId())
                .orElseGet(() -> {
                    StudentHourAccount newAccount = StudentHourAccount.builder()
                            .studentId(request.getStudentId())
                            .totalRecharged(BigDecimal.ZERO)
                            .totalConsumed(BigDecimal.ZERO)
                            .totalRefunded(BigDecimal.ZERO)
                            .remainingHours(BigDecimal.ZERO)
                            .paidHours(BigDecimal.ZERO)
                            .giftHours(BigDecimal.ZERO)
                            .experienceHours(BigDecimal.ZERO)
                            .activityHours(BigDecimal.ZERO)
                            .version(0)
                            .build();
                    return hourRepository.saveAccount(newAccount);
                });

        String hourSource = request.getHourSource() != null ? request.getHourSource() : "PAID";

        LocalDate finalExpireDate = expireDate;
        if (request.getHourSource() != null && !"PAID".equals(request.getHourSource())) {
            finalExpireDate = null;
        }

        BigDecimal beforeBalance = safe(account.getRemainingHours());
        BigDecimal amount = request.getAmount();

        switch (hourSource) {
            case "GIFT":
                account.setGiftHours(safe(account.getGiftHours()).add(amount));
                break;
            case "EXPERIENCE":
                account.setExperienceHours(safe(account.getExperienceHours()).add(amount));
                break;
            case "ACTIVITY":
                account.setActivityHours(safe(account.getActivityHours()).add(amount));
                break;
            case "PAID":
            default:
                account.setPaidHours(safe(account.getPaidHours()).add(amount));
                hourSource = "PAID";
                break;
        }

        account.setTotalRecharged(safe(account.getTotalRecharged()).add(amount));
        account.setRemainingHours(calcRemaining(account));

        BigDecimal afterBalance = safe(account.getRemainingHours());

        StudentHourAccount savedAccount = hourRepository.saveAccount(account);

        HourTransaction transaction = HourTransaction.builder()
                .transactionNo(("ADJUST".equals(transactionType) ? "TA" : "TR")
                        + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .studentId(request.getStudentId())
                .transactionType(transactionType)
                .changeAmount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .reason(request.getReason() != null ? request.getReason()
                        : "ADJUST".equals(transactionType) ? "管理员手动增加课时" : "课时充值")
                .operatorId(currentUser.getUserId())
                .operatorRole(currentUser.getRole())
                .hourSource(hourSource)
                .expireDate(finalExpireDate != null ? finalExpireDate.atStartOfDay() : null)
                .build();

        hourRepository.saveTransaction(transaction);
        log.info("课时增加: studentId={}, amount={}, type={}, hourSource={}, expireDate={}",
                request.getStudentId(), amount, transactionType, hourSource, finalExpireDate);

        return savedAccount;
    }
    
    @Transactional
    public HourTransaction consume(Long studentId, BigDecimal amount, Long lessonId, Long lessonStudentId, 
                                       String reason, CurrentUser currentUser) {
        StudentHourAccount account = hourRepository.findAccountByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND, "学员没有课时账户"));
        
        BigDecimal beforeBalance = safe(account.getRemainingHours());
        BigDecimal remainingTotal = beforeBalance;
        
        if (!allowNegativeBalance && remainingTotal.compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.HOUR_INSUFFICIENT, 
                    "课时不足，当前剩余: " + remainingTotal + "，需要: " + amount);
        }
        
        BigDecimal toConsume = amount;
        List<String> sourcesUsed = new ArrayList<>();
        List<BigDecimal> amountsUsed = new ArrayList<>();
        
        String[] priority = {"EXPERIENCE", "ACTIVITY", "GIFT", "PAID"};
        for (String source : priority) {
            if (toConsume.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal bucket = switch (source) {
                case "EXPERIENCE" -> safe(account.getExperienceHours());
                case "ACTIVITY" -> safe(account.getActivityHours());
                case "GIFT" -> safe(account.getGiftHours());
                case "PAID" -> safe(account.getPaidHours());
                default -> BigDecimal.ZERO;
            };
            if (bucket.compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal take = bucket.min(toConsume);
            switch (source) {
                case "EXPERIENCE" -> account.setExperienceHours(bucket.subtract(take));
                case "ACTIVITY" -> account.setActivityHours(bucket.subtract(take));
                case "GIFT" -> account.setGiftHours(bucket.subtract(take));
                case "PAID" -> account.setPaidHours(bucket.subtract(take));
            }
            sourcesUsed.add(source);
            amountsUsed.add(take);
            toConsume = toConsume.subtract(take);
        }
        
        if (toConsume.compareTo(BigDecimal.ZERO) > 0 && allowNegativeBalance) {
            BigDecimal deficit = toConsume;
            account.setPaidHours(safe(account.getPaidHours()).subtract(deficit));
            sourcesUsed.add("PAID");
            amountsUsed.add(deficit);
            toConsume = BigDecimal.ZERO;
        }
        
        account.setTotalConsumed(safe(account.getTotalConsumed()).add(amount));
        account.setRemainingHours(calcRemaining(account));
        
        BigDecimal afterBalance = safe(account.getRemainingHours());
        
        try {
            hourRepository.saveAccount(account);
        } catch (MybatisPlusException e) {
            log.warn("课时账户乐观锁冲突: studentId={}, version={}", studentId, account.getVersion());
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课时账户并发操作冲突，请重试");
        }
        
        String primarySource = sourcesUsed.isEmpty() ? "PAID" : sourcesUsed.get(0);
        String sourceBreakdown = encodeSourceBreakdown(sourcesUsed, amountsUsed);
        HourTransaction transaction = HourTransaction.builder()
                .transactionNo("TC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .studentId(studentId)
                .transactionType("CONSUME")
                .changeAmount(amount.negate())
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .lessonId(lessonId)
                .lessonStudentId(lessonStudentId)
                .reason(reason != null ? reason : "课程消耗")
                .operatorId(currentUser.getUserId())
                .operatorRole(currentUser.getRole())
                .hourSource(primarySource)
                .sourceBreakdown(sourceBreakdown)
                .build();
        
        HourTransaction savedTransaction = hourRepository.saveTransaction(transaction);
        log.info("课时消耗: studentId={}, amount={}, lessonId={}, sources={}", studentId, amount, lessonId, sourcesUsed);
        
        return savedTransaction;
    }
    
    @Transactional
    public StudentHourAccount refund(HourRefundRequest request, CurrentUser currentUser) {
        StudentHourAccount account = hourRepository.findAccountByStudentId(request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND, "学员没有课时账户"));
        
        BigDecimal beforeBalance = safe(account.getRemainingHours());
        BigDecimal paidHours = safe(account.getPaidHours());
        BigDecimal afterBalance = beforeBalance.subtract(request.getAmount());
        
        if (!allowNegativeBalance && paidHours.compareTo(request.getAmount()) < 0) {
            throw new BusinessException(ErrorCode.HOUR_NEGATIVE_NOT_ALLOWED, "可减少的付费课时不足");
        }
        
        account.setTotalRefunded(safe(account.getTotalRefunded()).add(request.getAmount()));
        account.setPaidHours(paidHours.subtract(request.getAmount()));
        account.setRemainingHours(calcRemaining(account));
        
        afterBalance = safe(account.getRemainingHours());
        StudentHourAccount savedAccount = hourRepository.saveAccount(account);
        
        HourTransaction transaction = HourTransaction.builder()
                .transactionNo("TF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .studentId(request.getStudentId())
                .transactionType("REFUND")
                .changeAmount(request.getAmount().negate())
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .reason(request.getReason() != null ? request.getReason() : "课时退款")
                .operatorId(currentUser.getUserId())
                .operatorRole(currentUser.getRole())
                .hourSource("PAID")
                .build();
        
        hourRepository.saveTransaction(transaction);
        log.info("课时退款: studentId={}, amount={}", request.getStudentId(), request.getAmount());
        
        return savedAccount;
    }
    
    public StudentHourAccount getAccount(Long studentId) {
        return hourRepository.findAccountByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND, "学员没有课时账户"));
    }
    
    public List<HourTransaction> getTransactions(Long studentId) {
        return hourRepository.findTransactionsByStudentId(studentId);
    }

    public PageResult<HourAccountResponse> listAccounts(String keyword, int pageNum, int pageSize) {
        int pn = Math.max(1, pageNum);
        int ps = Math.min(500, Math.max(1, pageSize));
        long total = hourRepository.countAccounts(keyword);
        List<Object[]> rows = hourRepository.listAccountsRaw(keyword, (pn - 1) * ps, ps);
        List<HourAccountResponse> list = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long id = (Long) r[0];
            Long studentId = (Long) r[1];
            if (studentId == null) {
                continue;
            }
            HourAccountResponse.HourAccountResponseBuilder b = HourAccountResponse.builder()
                    .id(id != null ? id : studentId)
                    .studentId(studentId)
                    .studentName((String) r[2])
                    .studentNo((String) r[3])
                    .parentName((String) r[4])
                    .parentPhone((String) r[5])
                    .totalHours((BigDecimal) r[6])
                    .usedHours((BigDecimal) r[7])
                    .remainingHours((BigDecimal) r[8])
                    .paidHours((BigDecimal) r[9])
                    .giftHours((BigDecimal) r[10])
                    .createTime((LocalDateTime) r[11]);
            list.add(b.build());
        }
        return PageResult.of(list, total, pn, ps);
    }

    @Transactional
    public StudentHourAccount refundByTransaction(Long transactionId, String reason, CurrentUser currentUser) {
        HourTransaction originalTransaction = hourRepository.findTransactionById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOUR_TRANSACTION_FAILED, "原消耗流水不存在"));

        if (!"CONSUME".equals(originalTransaction.getTransactionType())) {
            throw new BusinessException(ErrorCode.HOUR_TRANSACTION_FAILED, "只能对消耗流水进行退回");
        }

        if (originalTransaction.getRelatedTransactionId() != null) {
            throw new BusinessException(ErrorCode.HOUR_TRANSACTION_FAILED, "该流水已被退回");
        }

        StudentHourAccount account = hourRepository.findAccountByStudentId(originalTransaction.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND, "学员没有课时账户"));

        BigDecimal refundAmount = originalTransaction.getChangeAmount().abs();
        BigDecimal beforeBalance = safe(account.getRemainingHours());
        
        String source = originalTransaction.getHourSource() != null ? originalTransaction.getHourSource() : "PAID";
        restoreConsumedSources(account, originalTransaction, refundAmount, source);

        account.setTotalRefunded(safe(account.getTotalRefunded()).add(refundAmount));
        account.setRemainingHours(calcRemaining(account));

        BigDecimal afterBalance = safe(account.getRemainingHours());
        
        try {
            hourRepository.saveAccount(account);
        } catch (MybatisPlusException e) {
            log.warn("课时退款乐观锁冲突: transactionId={}, studentId={}", transactionId, originalTransaction.getStudentId());
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课时账户并发操作冲突，请重试");
        }
        
        HourTransaction refundTransaction = HourTransaction.builder()
                .transactionNo("TRF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .studentId(originalTransaction.getStudentId())
                .transactionType("REFUND")
                .changeAmount(refundAmount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .lessonId(originalTransaction.getLessonId())
                .lessonStudentId(originalTransaction.getLessonStudentId())
                .relatedTransactionId(originalTransaction.getId())
                .reason(reason != null ? reason : "课时退回")
                .operatorId(currentUser.getUserId())
                .operatorRole(currentUser.getRole())
                .hourSource(source)
                .sourceBreakdown(originalTransaction.getSourceBreakdown())
                .build();

        HourTransaction savedRefundTransaction = hourRepository.saveTransaction(refundTransaction);

        originalTransaction.setRelatedTransactionId(savedRefundTransaction.getId());
        hourRepository.saveTransaction(originalTransaction);

        log.info("课时退回: transactionId={}, studentId={}, amount={}, source={}", transactionId,
                originalTransaction.getStudentId(), refundAmount, source);

        return hourRepository.findAccountByStudentId(originalTransaction.getStudentId()).orElse(null);
    }

    private String encodeSourceBreakdown(List<String> sources, List<BigDecimal> amounts) {
        if (sources.isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            if (i > 0) {
                result.append(';');
            }
            result.append(sources.get(i)).append('=').append(amounts.get(i).toPlainString());
        }
        return result.toString();
    }

    private void restoreConsumedSources(StudentHourAccount account, HourTransaction transaction,
                                        BigDecimal refundAmount, String defaultSource) {
        String breakdown = transaction.getSourceBreakdown();
        if (breakdown == null || breakdown.isBlank()) {
            addToSource(account, defaultSource, refundAmount);
            return;
        }

        BigDecimal restored = BigDecimal.ZERO;
        try {
            for (String part : breakdown.split(";")) {
                String[] values = part.split("=", 2);
                if (values.length != 2) {
                    throw new IllegalArgumentException("invalid source breakdown");
                }
                BigDecimal amount = new BigDecimal(values[1]);
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("negative source amount");
                }
                addToSource(account, values[0], amount);
                restored = restored.add(amount);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.HOUR_TRANSACTION_FAILED, "原消耗流水的课时来源明细无效");
        }
        if (restored.compareTo(refundAmount) != 0) {
            throw new BusinessException(ErrorCode.HOUR_TRANSACTION_FAILED, "原消耗流水的课时来源明细不完整");
        }
    }

    private void addToSource(StudentHourAccount account, String source, BigDecimal amount) {
        switch (source) {
            case "EXPERIENCE" -> account.setExperienceHours(safe(account.getExperienceHours()).add(amount));
            case "ACTIVITY" -> account.setActivityHours(safe(account.getActivityHours()).add(amount));
            case "GIFT" -> account.setGiftHours(safe(account.getGiftHours()).add(amount));
            case "PAID" -> account.setPaidHours(safe(account.getPaidHours()).add(amount));
            default -> throw new IllegalArgumentException("unknown hour source");
        }
    }
}
