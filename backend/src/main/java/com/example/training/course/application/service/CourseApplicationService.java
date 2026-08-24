package com.example.training.course.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.course.application.dto.ClassGroupCreateRequest;
import com.example.training.course.application.dto.CourseCreateRequest;
import com.example.training.course.application.dto.CourseResponse;
import com.example.training.course.domain.model.ClassGroup;
import com.example.training.course.domain.model.Course;
import com.example.training.course.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseApplicationService {
    private static final Logger log = LoggerFactory.getLogger(CourseApplicationService.class);

    
    private final CourseRepository courseRepository;
    
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, CurrentUser currentUser) {
        courseRepository.findByCourseCode(request.getCourseCode())
                .ifPresent(c -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "课程编码已存在");
                });
        
        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .courseType(request.getCourseType())
                .defaultDurationMinutes(request.getDefaultDurationMinutes())
                .defaultConsumeHours(request.getDefaultConsumeHours())
                .description(request.getDescription())
                .status("ENABLED")
                .createdBy(currentUser.getUserId())
                .build();
        
        Course savedCourse = courseRepository.save(course);
        return convertToResponse(savedCourse);
    }
    
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        return convertToResponse(course);
    }
    
    public List<CourseResponse> getAllEnabledCourses() {
        return listCoursesPage(null, 1, Integer.MAX_VALUE).getList();
    }

    public PageResult<CourseResponse> listCoursesPage(String keyword, int pageNum, int pageSize) {
        List<Course> all = courseRepository.findAllEnabled();
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        List<CourseResponse> filtered = all.stream()
                .filter(c -> kw == null || kw.isEmpty()
                        || contains(c.getCourseName(), kw)
                        || contains(c.getCourseCode(), kw)
                        || contains(c.getCourseType(), kw)
                        || contains(c.getDescription(), kw)
                        || contains(c.getStatus(), kw))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(from + pageSize, filtered.size());
        List<CourseResponse> page = from >= filtered.size() ? new ArrayList<>() : new ArrayList<>(filtered.subList(from, to));
        return PageResult.of(page, filtered.size(), pageNum, pageSize);
    }

    private static boolean contains(String src, String lowerKw) {
        if (src == null || lowerKw == null) return false;
        return src.toLowerCase().contains(lowerKw);
    }
    
    @Transactional
    public void disableCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        course.setStatus("DISABLED");
        courseRepository.save(course);
        log.info("禁用课程: id={}, code={}", id, course.getCourseCode());
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        course.setDeletedAt(java.time.LocalDateTime.now());
        courseRepository.save(course);
        log.info("删除课程: id={}, code={}", id, course.getCourseCode());
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseCreateRequest request, CurrentUser currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (request.getCourseName() != null) {
            course.setCourseName(request.getCourseName());
        }
        if (request.getCourseType() != null) {
            course.setCourseType(request.getCourseType());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getDefaultDurationMinutes() != null) {
            course.setDefaultDurationMinutes(request.getDefaultDurationMinutes());
        }
        if (request.getDefaultConsumeHours() != null) {
            course.setDefaultConsumeHours(request.getDefaultConsumeHours());
        }
        course.setUpdatedBy(currentUser.getUserId());

        Course savedCourse = courseRepository.save(course);
        log.info("更新课程: id={}, code={}", id, savedCourse.getCourseCode());
        return convertToResponse(savedCourse);
    }
    
    @Transactional
    public ClassGroup createClassGroup(ClassGroupCreateRequest request, CurrentUser currentUser) {
        courseRepository.findClassGroupByCode(request.getClassCode())
                .ifPresent(c -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "班级编号已存在");
                });
        
        courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        
        ClassGroup classGroup = ClassGroup.builder()
                .classCode(request.getClassCode())
                .className(request.getClassName())
                .courseId(request.getCourseId())
                .primaryTeacherId(request.getPrimaryTeacherId())
                .maxStudents(request.getMaxStudents())
                .status("ACTIVE")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .remark(request.getRemark())
                .createdBy(currentUser.getUserId())
                .build();
        
        return courseRepository.saveClassGroup(classGroup);
    }
    
    private CourseResponse convertToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .courseType(course.getCourseType())
                .defaultDurationMinutes(course.getDefaultDurationMinutes())
                .defaultConsumeHours(course.getDefaultConsumeHours())
                .description(course.getDescription())
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .build();
    }
    
    public ClassGroup getClassGroup(Long id) {
        return courseRepository.findClassGroupById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASS_GROUP_NOT_FOUND));
    }
    
    public List<ClassGroup> listClassGroups() {
        return courseRepository.findAllClassGroups();
    }
    
    @Transactional
    public ClassGroup updateClassGroup(Long id, ClassGroupCreateRequest request, CurrentUser currentUser) {
        ClassGroup classGroup = courseRepository.findClassGroupById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASS_GROUP_NOT_FOUND));

        if (request.getClassName() != null) classGroup.setClassName(request.getClassName());
        if (request.getCourseId() != null) classGroup.setCourseId(request.getCourseId());
        if (request.getPrimaryTeacherId() != null) classGroup.setPrimaryTeacherId(request.getPrimaryTeacherId());
        if (request.getMaxStudents() != null) classGroup.setMaxStudents(request.getMaxStudents());
        if (request.getStatus() != null) classGroup.setStatus(request.getStatus());
        if (request.getStartDate() != null) classGroup.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) classGroup.setEndDate(request.getEndDate());
        if (request.getRemark() != null) classGroup.setRemark(request.getRemark());
        classGroup.setUpdatedBy(currentUser.getUserId());

        ClassGroup saved = courseRepository.saveClassGroup(classGroup);
        log.info("更新班级: id={}, name={}", id, saved.getClassName());
        return saved;
    }
    
    @Transactional
    public void addStudentsToClassGroup(Long classGroupId, List<Long> studentIds, com.example.training.common.security.CurrentUser currentUser) {
        ClassGroup classGroup = courseRepository.findClassGroupById(classGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASS_GROUP_NOT_FOUND));
        
        courseRepository.addStudentsToClassGroup(classGroupId, studentIds, currentUser.getUserId());
        log.info("添加学员到班级: classGroupId={}, studentIds={}", classGroupId, studentIds);
    }
    
    @Transactional
    public void removeStudentsFromClassGroup(Long classGroupId, List<Long> studentIds) {
        ClassGroup classGroup = courseRepository.findClassGroupById(classGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASS_GROUP_NOT_FOUND));
        
        courseRepository.removeStudentsFromClassGroup(classGroupId, studentIds);
        log.info("从班级移除学员: classGroupId={}, studentIds={}", classGroupId, studentIds);
    }
}
