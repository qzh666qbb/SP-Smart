package com.example.training.hour.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.dto.HourRechargeRequest;
import com.example.training.hour.application.dto.HourRefundRequest;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.hour.domain.model.StudentHourAccount;
import com.example.training.hour.domain.repository.HourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HourApplicationService {
    
    private final HourRepository hourRepository;
    
    @Value("${app.hour.allow-negative-balance:false}")
    private boolean allowNegativeBalance;
    
    @Transactional
    public StudentHourAccount recharge(HourRechargeRequest request, CurrentUser currentUser) {
        StudentHourAccount account = hourRepository.findAccountByStudentId(request.getStudentId())
                .orElseGet(() -> {
                    StudentHourAccount newAccount = StudentHourAccount.builder()
                            .studentId(request.getStudentId())
                            .totalRecharged(BigDecimal.ZERO)
                            .totalConsumed(BigDecimal.ZERO)
                            .totalRefunded(BigDecimal.ZERO)
                            .remainingHours(BigDecimal.ZERO)
                            .version(0)
                            .build();
                    return hourRepository.saveAccount(newAccount);
                });
        
        BigDecimal beforeBalance = account.getRemainingHours();
        BigDecimal afterBalance = beforeBalance.add(request.getAmount());
        
        account.setTotalRecharged(account.getTotalRecharged().add(request.getAmount()));
        account.setRemainingHours(afterBalance);
        account.setVersion(account.getVersion() + 1);
        
        StudentHourAccount savedAccount = hourRepository.saveAccount(account);
        
        HourTransaction transaction = HourTransaction.builder()
                .transactionNo("TR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .studentId(request.getStudentId())
                .transactionType("RECHARGE")
                .changeAmount(request.getAmount())
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .reason(request.getReason() != null ? request.getReason() : "课时充值")
                .operatorId(currentUser.getUserId())
                .operatorRole(currentUser.getRole())
                .build();
        
        hourRepository.saveTransaction(transaction);
        log.info("课时充值: studentId={}, amount={}", request.getStudentId(), request.getAmount());
        
        return savedAccount;
    }
    
    @Transactional
    public StudentHourAccount consume(Long studentId, BigDecimal amount, Long lessonId, Long lessonStudentId, 
                                       String reason, CurrentUser currentUser) {
        StudentHourAccount account = hourRepository.findAccountByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND, "学员没有课时账户"));
        
        BigDecimal beforeBalance = account.getRemainingHours();
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        
        if (!allowNegativeBalance && afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.HOUR_INSUFFICIENT, 
                    "课时不足，当前剩余: " + beforeBalance + "，需要: " + amount);
        }
        
        account.setTotalConsumed(account.getTotalConsumed().add(amount));
        account.setRemainingHours(afterBalance);
        account.setVersion(account.getVersion() + 1);
        
        StudentHourAccount savedAccount = hourRepository.saveAccount(account);
        
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
                .build();
        
        hourRepository.saveTransaction(transaction);
        log.info("课时消耗: studentId={}, amount={}, lessonId={}", studentId, amount, lessonId);
        
        return savedAccount;
    }
    
    @Transactional
    public StudentHourAccount refund(HourRefundRequest request, CurrentUser currentUser) {
        StudentHourAccount account = hourRepository.findAccountByStudentId(request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND, "学员没有课时账户"));
        
        BigDecimal beforeBalance = account.getRemainingHours();
        BigDecimal afterBalance = beforeBalance.subtract(request.getAmount());
        
        if (!allowNegativeBalance && afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.HOUR_NEGATIVE_NOT_ALLOWED, "不允许负余额");
        }
        
        account.setTotalRefunded(account.getTotalRefunded().add(request.getAmount()));
        account.setRemainingHours(afterBalance);
        account.setVersion(account.getVersion() + 1);
        
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
}
