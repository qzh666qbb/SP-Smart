package com.example.training.hour.domain.repository;

import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.hour.domain.model.StudentHourAccount;

import java.util.List;
import java.util.Optional;

public interface HourRepository {
    
    StudentHourAccount saveAccount(StudentHourAccount account);
    
    Optional<StudentHourAccount> findAccountByStudentId(Long studentId);
    
    StudentHourAccount getAccountForUpdate(Long studentId);
    
    HourTransaction saveTransaction(HourTransaction transaction);
    
    Optional<HourTransaction> findTransactionByNo(String transactionNo);
    
    Optional<HourTransaction> findTransactionByKey(String idempotencyKey);

    Optional<HourTransaction> findTransactionById(Long id);

    List<HourTransaction> findTransactionsByStudentId(Long studentId);
    
    List<HourTransaction> findTransactionsByType(String transactionType);

    List<Object[]> listAccountsRaw(String keyword, int offset, int pageSize);

    long countAccounts(String keyword);
}
