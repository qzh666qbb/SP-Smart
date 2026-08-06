package com.example.training.hour.domain.repository;

import com.example.training.hour.domain.model.HourPackage;
import com.example.training.hour.domain.model.StudentPackageRecord;

import java.util.List;
import java.util.Optional;

public interface HourPackageRepository {

    HourPackage savePackage(HourPackage hourPackage);

    Optional<HourPackage> findPackageById(Long id);

    List<HourPackage> listAllPackages();

    StudentPackageRecord savePurchaseRecord(StudentPackageRecord record);

    List<StudentPackageRecord> findRecordsByStudentId(Long studentId);
}
