package com.example.training.hour.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.dto.HourPackageCreateRequest;
import com.example.training.hour.application.dto.HourPackageUpdateRequest;
import com.example.training.hour.application.dto.HourRechargeRequest;
import com.example.training.hour.domain.model.HourPackage;
import com.example.training.hour.domain.model.StudentPackageRecord;
import com.example.training.hour.domain.repository.HourPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class HourPackageService {

    private final HourPackageRepository hourPackageRepository;
    private final HourApplicationService hourApplicationService;

    private final Random random = new Random();

    @Transactional
    public HourPackage createPackage(HourPackageCreateRequest dto, CurrentUser currentUser) {
        String packageCode = "PKG" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));

        HourPackage hourPackage = HourPackage.builder()
                .packageCode(packageCode)
                .packageName(dto.getPackageName())
                .packageType(dto.getPackageType() != null ? dto.getPackageType() : "PAID")
                .totalHours(dto.getTotalHours())
                .price(dto.getPrice())
                .validMonths(dto.getValidMonths() != null ? dto.getValidMonths() : 12)
                .description(dto.getDescription())
                .status("ENABLED")
                .createdBy(currentUser.getUserId())
                .build();

        return hourPackageRepository.savePackage(hourPackage);
    }

    @Transactional
    public HourPackage updatePackage(Long id, HourPackageUpdateRequest dto, CurrentUser currentUser) {
        HourPackage hourPackage = hourPackageRepository.findPackageById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "课包不存在"));

        if (dto.getPackageName() != null) {
            hourPackage.setPackageName(dto.getPackageName());
        }
        if (dto.getPackageType() != null) {
            hourPackage.setPackageType(dto.getPackageType());
        }
        if (dto.getTotalHours() != null) {
            hourPackage.setTotalHours(dto.getTotalHours());
        }
        if (dto.getPrice() != null) {
            hourPackage.setPrice(dto.getPrice());
        }
        if (dto.getValidMonths() != null) {
            hourPackage.setValidMonths(dto.getValidMonths());
        }
        if (dto.getDescription() != null) {
            hourPackage.setDescription(dto.getDescription());
        }
        hourPackage.setUpdatedBy(currentUser.getUserId());

        return hourPackageRepository.savePackage(hourPackage);
    }

    @Transactional
    public HourPackage disablePackage(Long id, CurrentUser currentUser) {
        HourPackage hourPackage = hourPackageRepository.findPackageById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "课包不存在"));

        hourPackage.setStatus("DISABLED");
        hourPackage.setUpdatedBy(currentUser.getUserId());

        return hourPackageRepository.savePackage(hourPackage);
    }

    @Transactional
    public StudentPackageRecord purchasePackage(Long studentId, Long packageId, CurrentUser currentUser) {
        HourPackage hourPackage = hourPackageRepository.findPackageById(packageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "课包不存在"));

        if (!hourPackage.isEnabled()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课包已禁用");
        }

        LocalDate purchaseDate = LocalDate.now();
        int validMonths = hourPackage.getValidMonths() != null ? hourPackage.getValidMonths() : 12;
        LocalDate expireDate = purchaseDate.plusMonths(validMonths);

        StudentPackageRecord record = StudentPackageRecord.builder()
                .studentId(studentId)
                .packageId(packageId)
                .purchaseDate(purchaseDate)
                .expireDate(expireDate)
                .totalHours(hourPackage.getTotalHours())
                .remainingHours(hourPackage.getTotalHours())
                .consumedHours(java.math.BigDecimal.ZERO)
                .refundedHours(java.math.BigDecimal.ZERO)
                .status("ACTIVE")
                .createdBy(currentUser.getUserId())
                .build();

        StudentPackageRecord savedRecord = hourPackageRepository.savePurchaseRecord(record);

        HourRechargeRequest rechargeRequest = HourRechargeRequest.builder()
                .studentId(studentId)
                .amount(hourPackage.getTotalHours())
                .reason("购买课包" + hourPackage.getPackageName())
                .hourSource(hourPackage.getPackageType())
                .build();

        hourApplicationService.recharge(rechargeRequest, currentUser, expireDate);

        log.info("购买课包: studentId={}, packageId={}, recordId={}", studentId, packageId, savedRecord.getId());
        return savedRecord;
    }

    public List<HourPackage> listAllPackages() {
        return hourPackageRepository.listAllPackages();
    }

    public List<StudentPackageRecord> getStudentPackages(Long studentId) {
        return hourPackageRepository.findRecordsByStudentId(studentId);
    }
}
