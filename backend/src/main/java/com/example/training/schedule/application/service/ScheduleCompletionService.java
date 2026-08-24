package com.example.training.schedule.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.service.HourApplicationService;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleCompletionService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleCompletionService.class);


    private final ScheduleRepository scheduleRepository;
    private final HourApplicationService hourApplicationService;

    @Transactional
    public void completeLessonWithAutoConsume(Long lessonId, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.isCompleted() || lesson.isCancelled()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程已完成或已取消，不能重复完成");
        }

        lesson.setStatus("COMPLETED");
        lesson.setCompletedAt(LocalDateTime.now());
        lesson.setCompletedBy(currentUser.getUserId());
        lesson.setUpdatedBy(currentUser.getUserId());

        try {
            scheduleRepository.saveLesson(lesson);
        } catch (MybatisPlusException e) {
            log.warn("乐观锁冲突，课程已被其他请求完成: lessonId={}", lessonId);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程已完成，不能重复完成");
        } catch (DataAccessException e) {
            log.warn("数据访问冲突，可能课程已被并发修改: lessonId={}", lessonId);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程状态已变更，操作不允许");
        }

        List<LessonStudent> lessonStudents = scheduleRepository.findLessonStudentsByLessonId(lessonId);

        for (LessonStudent ls : lessonStudents) {
            if ("PRESENT".equals(ls.getAttendanceStatus()) && !ls.getShouldConsume()) {
                BigDecimal consumeAmount = ls.getConsumeHours() != null
                        ? ls.getConsumeHours()
                        : lesson.getConsumeHours() != null
                            ? lesson.getConsumeHours()
                            : BigDecimal.ONE;

                if (consumeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    HourTransaction transaction = hourApplicationService.consume(
                            ls.getStudentId(), consumeAmount, lessonId, ls.getId(),
                            "课程消耗 - " + lesson.getLessonNo(), currentUser);

                    ls.setShouldConsume(true);
                    ls.setConsumeHours(consumeAmount);
                    ls.setHourTransactionId(transaction.getId());
                    scheduleRepository.saveLessonStudent(ls);

                    log.info("自动消课: studentId={}, lessonId={}, amount={}",
                            ls.getStudentId(), lessonId, consumeAmount);
                } else {
                    log.warn("跳过0课时消课: studentId={}, lessonId={}",
                            ls.getStudentId(), lessonId);
                }
            }
        }
    }

    @Transactional
    public void cancelLessonWithRefund(Long lessonId, String reason, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.isCancelled()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程已取消");
        }

        lesson.setStatus("CANCELLED");
        lesson.setCancelReason(reason);
        lesson.setUpdatedBy(currentUser.getUserId());

        try {
            scheduleRepository.saveLesson(lesson);
        } catch (MybatisPlusException e) {
            log.warn("乐观锁冲突，课程已被其他请求取消: lessonId={}", lessonId);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程已取消，不能重复操作");
        } catch (DataAccessException e) {
            log.warn("数据访问冲突，可能课程已被并发修改: lessonId={}", lessonId);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程状态已变更，操作不允许");
        }

        List<LessonStudent> lessonStudents = scheduleRepository.findLessonStudentsByLessonId(lessonId);

        for (LessonStudent ls : lessonStudents) {
            if (ls.getShouldConsume() && ls.getHourTransactionId() != null) {
                hourApplicationService.refundByTransaction(
                        ls.getHourTransactionId(), "课程取消退回 - " + lesson.getLessonNo(), currentUser);
                log.info("消课退回: studentId={}, lessonId={}", ls.getStudentId(), lessonId);
            }
        }
    }
}
