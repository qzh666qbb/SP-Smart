package com.example.training.teacher.application.service;

import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.teacher.application.dto.TeacherApplyReviewRequest;
import com.example.training.teacher.application.dto.TeacherApplySubmitRequest;
import com.example.training.teacher.application.dto.TeacherPreRegisterRequest;
import com.example.training.teacher.domain.model.TeacherApply;
import com.example.training.teacher.domain.model.TeacherPreRegister;
import com.example.training.teacher.domain.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherApplicationService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-temp-password:123456}")
    private String defaultTempPassword;

    @Transactional
    public Long submitApply(TeacherApplySubmitRequest req) {
        teacherRepository.findPreRegisterByPhone(req.getPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_PHONE_NOT_PRE_REGISTERED));

        teacherRepository.findApplyByPhone(req.getPhone())
                .ifPresent(existing -> {
                    if ("PENDING".equals(existing.getStatus())) {
                        throw new BusinessException(ErrorCode.TEACHER_APPLY_PENDING);
                    }
                });

        TeacherApply apply = TeacherApply.builder()
                .phone(req.getPhone())
                .realName(req.getRealName())
                .wechatOpenid(req.getWechatOpenid())
                .status("PENDING")
                .build();

        TeacherApply saved = teacherRepository.saveApply(apply);
        log.info("教师申请提交成功: applyId={}, phone={}", saved.getId(), saved.getPhone());
        return saved.getId();
    }

    public PageResult<Map<String, Object>> listApplies(String keyword, String status, int pageNum, int pageSize) {
        List<TeacherApply> list = teacherRepository.findApplies(keyword, status, pageNum, pageSize);
        long total = teacherRepository.countApplies(keyword, status);
        List<Map<String, Object>> records = list.stream().map(this::convertApplyToMap).collect(Collectors.toList());
        return PageResult.of(records, total, pageNum, pageSize);
    }

    @Transactional
    public Map<String, Object> reviewApply(Long id, TeacherApplyReviewRequest req, CurrentUser currentUser) {
        TeacherApply apply = teacherRepository.findApplyById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_APPLY_NOT_FOUND));

        if (Boolean.TRUE.equals(req.getApprove())) {
            apply.setStatus("APPROVED");
            apply.setReviewedAt(LocalDateTime.now());
            apply.setReviewerId(currentUser.getUserId());
            apply.setReviewComment(req.getComment());
            teacherRepository.saveApply(apply);

            Long userId = handleApproveCreateOrUpdateUser(apply);
            log.info("教师申请通过: applyId={}, phone={}, userId={}", id, apply.getPhone(), userId);

            Map<String, Object> result = new HashMap<>();
            result.put("applyId", apply.getId());
            result.put("userId", userId);
            return result;
        } else {
            apply.setStatus("REJECTED");
            apply.setReviewedAt(LocalDateTime.now());
            apply.setReviewerId(currentUser.getUserId());
            apply.setReviewComment(req.getComment());
            teacherRepository.saveApply(apply);
            log.info("教师申请拒绝: applyId={}, phone={}", id, apply.getPhone());

            Map<String, Object> result = new HashMap<>();
            result.put("applyId", apply.getId());
            return result;
        }
    }

    private Long handleApproveCreateOrUpdateUser(TeacherApply apply) {
        // 1) openid 命中已有账号：通常是 loginByWechat 提前自动建了 PARENT（升级角色），或已有 TEACHER
        User existingByOpenid = apply.getWechatOpenid() != null && !apply.getWechatOpenid().isBlank()
                ? userRepository.findByWechatOpenid(apply.getWechatOpenid()).orElse(null)
                : null;
        // 2) 手机命中已有账号
        User existingByPhone = userRepository.findByPhone(apply.getPhone()).orElse(null);

        // 如果两个账号同时存在且不同实体 → 优先用 phone 对应的账号，把 openid 账号合并（清 openid 关联，避免 DUPLICATE KEY）
        if (existingByOpenid != null && existingByPhone != null && !existingByOpenid.getId().equals(existingByPhone.getId())) {
            existingByOpenid.setWechatOpenid(null);
            existingByOpenid.setWechatUnionid(null);
            userRepository.save(existingByOpenid);
            existingByOpenid = existingByPhone;
        }

        User target = existingByPhone != null ? existingByPhone : existingByOpenid;

        if (target != null) {
            // 升级/复用为 TEACHER
            if (!"TEACHER".equals(target.getRole())) {
                target.setRole("TEACHER");
                // 仅 PARENT/小程序预注册可能没有密码 → 给默认密码
                if (target.getPasswordHash() == null || target.getPasswordHash().isBlank()) {
                    target.setPasswordHash(passwordEncoder.encode(defaultTempPassword));
                }
            }
            target.setPhone(apply.getPhone());
            if (apply.getWechatOpenid() != null && !apply.getWechatOpenid().isBlank()) {
                target.setWechatOpenid(apply.getWechatOpenid());
            }
            if (target.getStatus() == null || target.getStatus().isBlank()) {
                target.setStatus("ENABLED");
            }
            target.setPasswordUpdatedAt(LocalDateTime.now());
            User saved = userRepository.save(target);
            // 确保 teacher_profile 存在
            TeacherProfile profile = userRepository.findTeacherProfileByUserId(saved.getId()).orElse(null);
            if (profile == null) {
                String teacherNo = "T" + String.format("%08d", System.currentTimeMillis() % 100000000)
                        + UUID.randomUUID().toString().substring(0, 3).toUpperCase();
                profile = TeacherProfile.builder()
                        .userId(saved.getId())
                        .teacherNo(teacherNo)
                        .name(apply.getRealName())
                        .employmentStatus("ACTIVE")
                        .joinedDate(LocalDate.now())
                        .build();
                userRepository.saveTeacherProfile(profile);
            } else if (!apply.getRealName().equals(profile.getName())) {
                profile.setName(apply.getRealName());
                userRepository.saveTeacherProfile(profile);
            }
            return saved.getId();
        }

        // 3) 没有任何账号 → 新建 TEACHER
        String username = "teacher_" + apply.getPhone();
        User newUser = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(defaultTempPassword))
                .role("TEACHER")
                .phone(apply.getPhone())
                .wechatOpenid(apply.getWechatOpenid())
                .status("ENABLED")
                .passwordUpdatedAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(newUser);

        String teacherNo = "T" + String.format("%08d", System.currentTimeMillis() % 100000000)
                + UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        TeacherProfile profile = TeacherProfile.builder()
                .userId(savedUser.getId())
                .teacherNo(teacherNo)
                .name(apply.getRealName())
                .employmentStatus("ACTIVE")
                .joinedDate(LocalDate.now())
                .build();
        userRepository.saveTeacherProfile(profile);

        return savedUser.getId();
    }

    @Transactional
    public Long preRegister(TeacherPreRegisterRequest req, CurrentUser currentUser) {
        TeacherPreRegister existing = teacherRepository.findPreRegisterByPhone(req.getPhone()).orElse(null);
        if (existing != null) {
            if (!"ACTIVE".equals(existing.getStatus())) {
                existing.setStatus("ACTIVE");
                existing.setRealName(req.getRealName());
                teacherRepository.savePreRegister(existing);
            }
            return existing.getId();
        }

        TeacherPreRegister preRegister = TeacherPreRegister.builder()
                .phone(req.getPhone())
                .realName(req.getRealName())
                .createdBy(currentUser.getUserId())
                .status("ACTIVE")
                .build();
        TeacherPreRegister saved = teacherRepository.savePreRegister(preRegister);
        log.info("教师预登记成功: id={}, phone={}", saved.getId(), saved.getPhone());
        return saved.getId();
    }

    public PageResult<Map<String, Object>> listPreRegisters(String keyword, int pageNum, int pageSize) {
        List<TeacherPreRegister> list = teacherRepository.findPreRegisters(keyword, pageNum, pageSize);
        long total = teacherRepository.countPreRegisters(keyword);
        List<Map<String, Object>> records = list.stream().map(this::convertPreRegisterToMap).collect(Collectors.toList());
        return PageResult.of(records, total, pageNum, pageSize);
    }

    private Map<String, Object> convertApplyToMap(TeacherApply a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("phone", a.getPhone());
        m.put("realName", a.getRealName());
        m.put("wechatOpenid", a.getWechatOpenid());
        m.put("status", a.getStatus());
        m.put("reviewerId", a.getReviewerId());
        m.put("reviewComment", a.getReviewComment());
        m.put("reviewedAt", a.getReviewedAt());
        m.put("createdBy", a.getCreatedBy());
        m.put("createdAt", a.getCreatedAt());
        m.put("updatedAt", a.getUpdatedAt());
        return m;
    }

    private Map<String, Object> convertPreRegisterToMap(TeacherPreRegister p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("phone", p.getPhone());
        m.put("realName", p.getRealName());
        m.put("createdBy", p.getCreatedBy());
        m.put("status", p.getStatus());
        m.put("createdAt", p.getCreatedAt());
        m.put("updatedAt", p.getUpdatedAt());
        return m;
    }
}
