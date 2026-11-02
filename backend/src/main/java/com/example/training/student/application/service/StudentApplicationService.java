package com.example.training.student.application.service;

import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.domain.model.StudentHourAccount;
import com.example.training.hour.domain.repository.HourRepository;
import com.example.training.student.application.dto.BindRequest;
import com.example.training.student.application.dto.ChildCreateRequest;
import com.example.training.student.application.dto.StudentCreateRequest;
import com.example.training.student.application.dto.StudentResponse;
import com.example.training.student.domain.model.ParentStudentRelation;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(StudentApplicationService.class);
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final HourRepository hourRepository;

    @Value("${app.student.max-children-per-parent:6}")
    private int maxChildrenPerParent;

    @Transactional
    public StudentResponse createChildByParent(CurrentUser currentUser, ChildCreateRequest req) {
        ParentProfile parentProfile = userRepository.findParentProfileByUserId(currentUser.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "家长资料不存在，请先完善家长信息"));

        if (parentProfile.getId() == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "家长资料异常，请重新完善家长信息");
        }
        Long parentId = parentProfile.getId();

        List<Student> existingChildren = studentRepository.findByParentId(parentId);
        if (existingChildren.size() >= maxChildrenPerParent) {
            throw new BusinessException(ErrorCode.CHILD_MAX_LIMIT);
        }

        String studentNo = generateStudentNo();

        Student student = Student.builder()
                .studentNo(studentNo)
                .name(req.getName())
                .gender(req.getGender() != null ? req.getGender() : "UNKNOWN")
                .birthDate(req.getBirthDate())
                .phone(req.getPhone())
                .parentName(parentProfile.getName())
                .parentPhone(parentProfile.getPhone())
                .status("ACTIVE")
                .enrollmentDate(LocalDate.now())
                .medicalNotes(req.getMedicalNotes())
                .remark(req.getRemark())
                .createdBy(currentUser.getUserId())
                .build();

        Student savedStudent = studentRepository.save(student);

        String relationType = normalizeRelation(parentProfile.getRelationRemark());
        boolean isPrimary = existingChildren.isEmpty();

        ParentStudentRelation relation = ParentStudentRelation.builder()
                .parentId(parentId)
                .studentId(savedStudent.getId())
                .relationType(relationType)
                .isPrimary(isPrimary)
                .bindStatus("CONFIRMED")
                .boundAt(LocalDateTime.now())
                .createdBy(currentUser.getUserId())
                .build();
        studentRepository.saveRelation(relation);

        ensureHourAccount(savedStudent.getId());

        log.info("家长自建孩子成功: parentId={}, studentId={}, studentNo={}",
                parentId, savedStudent.getId(), savedStudent.getStudentNo());

        return convertToResponse(savedStudent);
    }

    private String generateStudentNo() {
        Set<Integer> usedNumbers = new HashSet<>();
        for (Student student : studentRepository.findAll()) {
            String no = student.getStudentNo();
            if (no != null && no.matches("\\d+")) {
                try {
                    int value = Integer.parseInt(no);
                    if (value > 0) usedNumbers.add(value);
                } catch (NumberFormatException ignore) {
                    // 超出整数范围的旧学号不参与顺序号生成。
                }
            }
        }
        int next = 1;
        while (usedNumbers.contains(next)) next++;
        return String.format("%03d", next);
    }

    private String normalizeRelation(String raw) {
        if (raw == null) return "OTHER";
        String upper = raw.trim().toUpperCase();
        switch (upper) {
            case "FATHER":
            case "MOTHER":
            case "GUARDIAN":
            case "OTHER":
                return upper;
            default:
                return "OTHER";
        }
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request, CurrentUser currentUser) {
        String studentNo = generateStudentNo();
        Long primaryTeacherId = currentUser.isTeacher() ? currentUser.getProfileId() : request.getPrimaryTeacherId();
        validateTeacherProfile(primaryTeacherId);

        Student student = Student.builder()
                .studentNo(studentNo)
                .name(request.getName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phone(request.getPhone())
                .parentName(request.getParentName())
                .parentPhone(request.getParentPhone())
                .parentRelation(request.getParentRelation())
                .primaryTeacherId(primaryTeacherId)
                .enrollmentDate(request.getEnrollmentDate())
                .status("ACTIVE")
                .medicalNotes(request.getMedicalNotes())
                .remark(request.getRemark())
                .createdBy(currentUser.getUserId())
                .build();

        Student savedStudent = studentRepository.save(student);
        ensureHourAccount(savedStudent.getId());
        return convertToResponse(savedStudent);
    }

    /**
     * Every student owns an hour account from creation. Initial hours stay at zero;
     * administrators add hours later through the recharge flow so a transaction is recorded.
     */
    private StudentHourAccount ensureHourAccount(Long studentId) {
        return hourRepository.findAccountByStudentId(studentId)
                .orElseGet(() -> hourRepository.saveAccount(StudentHourAccount.builder()
                        .studentId(studentId)
                        .totalRecharged(BigDecimal.ZERO)
                        .totalConsumed(BigDecimal.ZERO)
                        .totalRefunded(BigDecimal.ZERO)
                        .remainingHours(BigDecimal.ZERO)
                        .paidHours(BigDecimal.ZERO)
                        .giftHours(BigDecimal.ZERO)
                        .experienceHours(BigDecimal.ZERO)
                        .activityHours(BigDecimal.ZERO)
                        .version(0)
                        .build()));
    }

    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        return convertToResponse(student);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentCreateRequest request, CurrentUser currentUser) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getGender() != null) existing.setGender(request.getGender());
        if (request.getBirthDate() != null) existing.setBirthDate(request.getBirthDate());
        if (request.getPhone() != null) existing.setPhone(request.getPhone());
        if (request.getParentName() != null) existing.setParentName(request.getParentName());
        if (request.getParentPhone() != null) existing.setParentPhone(request.getParentPhone());
        if (request.getParentRelation() != null) existing.setParentRelation(request.getParentRelation());
        if (currentUser.isAdmin()) {
            validateTeacherProfile(request.getPrimaryTeacherId());
            existing.setPrimaryTeacherId(request.getPrimaryTeacherId());
        }
        if (request.getEnrollmentDate() != null) existing.setEnrollmentDate(request.getEnrollmentDate());
        if (request.getMedicalNotes() != null) existing.setMedicalNotes(request.getMedicalNotes());
        if (request.getRemark() != null) existing.setRemark(request.getRemark());
        existing.setUpdatedBy(currentUser.getUserId());

        Student saved = studentRepository.save(existing);
        return convertToResponse(saved);
    }

    private void validateTeacherProfile(Long teacherProfileId) {
        if (teacherProfileId != null && userRepository.findTeacherProfileById(teacherProfileId).isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "负责教练不存在或未建立教师档案");
        }
    }

    public List<StudentResponse> getAllStudents() {
        return getAllStudentsPage(null, null, 1, Integer.MAX_VALUE).getList();
    }

    public PageResult<StudentResponse> getAllStudentsPage(String keyword, Long teacherId, int pageNum, int pageSize) {
        int pn = Math.max(1, pageNum);
        int ps = Math.min(500, Math.max(1, pageSize));
        List<Student> all = teacherId != null
                ? studentRepository.findByTeacherId(teacherId)
                : studentRepository.findAll();
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        List<StudentResponse> filtered = all.stream()
                .filter(s -> kw == null || kw.isEmpty()
                        || contains(s.getName(), kw)
                        || contains(s.getStudentNo(), kw)
                        || contains(s.getParentName(), kw)
                        || contains(s.getParentPhone(), kw)
                        || contains(s.getPhone(), kw)
                        || contains(s.getGender(), kw))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        int from = Math.max(0, (pn - 1) * ps);
        int to = Math.min(from + ps, filtered.size());
        List<StudentResponse> page = from >= filtered.size() ? new ArrayList<>() : new ArrayList<>(filtered.subList(from, to));
        return PageResult.of(page, filtered.size(), pn, ps);
    }

    public List<StudentResponse> getStudentsByTeacher(Long teacherId) {
        return getAllStudentsPage(null, teacherId, 1, Integer.MAX_VALUE).getList();
    }

    private static boolean contains(String src, String lowerKw) {
        if (src == null || lowerKw == null) return false;
        return src.toLowerCase().contains(lowerKw);
    }

    public List<StudentResponse> getStudentsByParent(Long parentId) {
        List<Student> students = studentRepository.findByParentId(parentId);
        return students.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void bindStudent(BindRequest request, CurrentUser currentUser) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        Long parentId = requireParentProfileId(currentUser);
        studentRepository.findRelation(parentId, request.getStudentId())
                .ifPresent(relation -> {
                    if ("PENDING".equals(relation.getBindStatus())) {
                        throw new BusinessException(ErrorCode.STUDENT_BIND_PENDING, "绑定请求已存在");
                    }
                });

        ParentStudentRelation relation = ParentStudentRelation.builder()
                .parentId(parentId)
                .studentId(request.getStudentId())
                .relationType(request.getRelationType())
                .isPrimary(false)
                .bindStatus("PENDING")
                .createdBy(currentUser.getUserId())
                .build();

        studentRepository.saveRelation(relation);
        log.info("家长绑定学员: parentId={}, studentId={}", parentId, request.getStudentId());
    }

    @Transactional
    public void confirmBind(Long relationId, CurrentUser currentUser) {
        Long parentId = requireParentProfileId(currentUser);
        ParentStudentRelation relation = studentRepository.findRelationById(relationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "绑定关系不存在"));
        if (!parentId.equals(relation.getParentId())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限确认该绑定关系");
        }

        relation.setBindStatus("CONFIRMED");
        relation.setBoundAt(LocalDateTime.now());
        studentRepository.saveRelation(relation);
        log.info("确认绑定: relationId={}", relationId);
    }

    @Transactional
    public void unbindStudent(Long studentId, CurrentUser currentUser) {
        Long parentId = requireParentProfileId(currentUser);
        ParentStudentRelation relation = studentRepository.findRelation(parentId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "绑定关系不存在"));

        relation.setBindStatus("UNBOUND");
        studentRepository.saveRelation(relation);
        log.info("解除绑定: parentId={}, studentId={}", parentId, studentId);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (student.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        student.setStudentNo(buildDeletedStudentNo(student));
        student.setDeletedAt(LocalDateTime.now());
        studentRepository.save(student);
        log.info("删除学员: id={}, name={}", id, student.getName());
    }

    private String buildDeletedStudentNo(Student student) {
        String original = student.getStudentNo() == null ? "" : student.getStudentNo().trim();
        String prefix = "DEL_" + student.getId() + "_";
        int maxOriginalLen = Math.max(0, 32 - prefix.length());
        return prefix + (original.length() > maxOriginalLen ? original.substring(0, maxOriginalLen) : original);
    }

    public StudentResponse matchStudentForBind(CurrentUser currentUser, com.example.training.student.application.dto.ParentBindMatchRequest request) {
        String no = request.getStudentNo() == null ? "" : request.getStudentNo().trim();
        String nm = request.getStudentName() == null ? "" : request.getStudentName().trim();
        String pp = request.getParentPhone() == null ? "" : request.getParentPhone().trim();
        if (no.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "孩子学号不能为空");
        }
        if (nm.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "孩子姓名不能为空");
        }
        if (pp.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "家长手机号不能为空");
        }
        if (!pp.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "家长手机号格式不正确");
        }
        Student student = studentRepository.findByBindMatch(no, nm, pp)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND, "未找到匹配的学员，请确认学号、姓名、家长手机号填写正确"));
        log.info("家长绑定匹配成功: parentId={}, studentId={}", currentUser.getUserId(), student.getId());
        return convertToResponse(student);
    }

    @Transactional
    public void parentConfirmBind(Long studentId, String relationType, CurrentUser currentUser) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        if (!"ADMIN".equals(currentUser.getRole()) && !"PARENT".equals(currentUser.getRole())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限操作");
        }

        if ("PARENT".equals(currentUser.getRole())) {
            String storedParentPhone = student.getParentPhone();
            String storedPhone = student.getPhone();
            String reqPhone = currentUser.getPhone() == null ? "" : currentUser.getPhone();
            boolean phoneOk =
                    (storedParentPhone != null && storedParentPhone.equals(reqPhone)) ||
                            (storedPhone != null && storedPhone.equals(reqPhone));
            if (!phoneOk) {
                log.warn("家长尝试绑定非自己子女: parentId={}, studentId={}", currentUser.getUserId(), studentId);
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该学生信息与家长账号不符，请先完成匹配");
            }
        }

        Long parentId = requireParentProfileId(currentUser);
        studentRepository.findRelation(parentId, studentId)
                .ifPresent(r -> {
                    if ("PENDING".equals(r.getBindStatus()) || "CONFIRMED".equals(r.getBindStatus())) {
                        throw new BusinessException(ErrorCode.STUDENT_BIND_PENDING, "您已经绑定过该学员");
                    }
                });

        ParentStudentRelation relation = ParentStudentRelation.builder()
                .parentId(parentId)
                .studentId(studentId)
                .relationType(relationType)
                .isPrimary(false)
                .bindStatus("CONFIRMED")
                .boundAt(java.time.LocalDateTime.now())
                .createdBy(currentUser.getUserId())
                .build();
        studentRepository.saveRelation(relation);
        log.info("家长确认绑定学员成功: parentId={}, studentId={}", parentId, studentId);
    }

    private Long requireParentProfileId(CurrentUser currentUser) {
        if (currentUser == null || currentUser.getProfileId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "家长资料不存在，请先完善资料");
        }
        return currentUser.getProfileId();
    }

    public StudentResponse getChildById(Long parentId, Long studentId) {
        ParentStudentRelation relation = studentRepository.findRelation(parentId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该孩子信息"));
        if (!"CONFIRMED".equals(relation.getBindStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该绑定关系尚未确认");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        return convertToResponse(student);
    }

    private StudentResponse convertToResponse(Student student) {
        String primaryTeacherName = student.getPrimaryTeacherId() == null ? null
                : userRepository.findTeacherProfileById(student.getPrimaryTeacherId())
                        .map(profile -> profile.getName()).orElse(null);
        return StudentResponse.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gender(student.getGender())
                .birthDate(student.getBirthDate())
                .age(student.getAge())
                .phone(student.getPhone())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .parentRelation(student.getParentRelation())
                .primaryTeacherId(student.getPrimaryTeacherId())
                .primaryTeacherName(primaryTeacherName)
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .medicalNotes(student.getMedicalNotes())
                .remark(student.getRemark())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
