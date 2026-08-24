package com.example.training.teacher.domain.repository;

import com.example.training.teacher.domain.model.TeacherApply;
import com.example.training.teacher.domain.model.TeacherPreRegister;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository {

    TeacherApply saveApply(TeacherApply apply);

    Optional<TeacherApply> findApplyById(Long id);

    Optional<TeacherApply> findApplyByPhone(String phone);

    Optional<TeacherApply> findApplyByOpenid(String openid);

    Optional<TeacherApply> findApplyByPhoneOrOpenid(String phone, String openid);

    List<TeacherApply> findApplies(String keyword, String status, int pageNum, int pageSize);

    long countApplies(String keyword, String status);

    TeacherPreRegister savePreRegister(TeacherPreRegister preRegister);

    Optional<TeacherPreRegister> findPreRegisterByPhone(String phone);

    List<TeacherPreRegister> findPreRegisters(String keyword, int pageNum, int pageSize);

    long countPreRegisters(String keyword);
}
