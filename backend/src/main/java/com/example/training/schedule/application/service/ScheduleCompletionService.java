package com.example.training.schedule.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.service.HourApplicationService;
import com.example.training.hour.domain.model.StudentHourAccount;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleCompletionService {

    private final ScheduleRepository scheduleRepository;
    private final HourApplicationService hourApplicationService;

    @Transactional
    public void completeLessonWithAutoConsume(Long lessonId, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.isCompleted()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程已完成");
        }

        lesson.setStatus("COMPLETED");
        lesson.setCompletedAt(LocalDateTime.now());
        lesson.setCompletedBy(currentUser.getUserId());
        lesson.setUpdatedBy(currentUser.getUserId());
        scheduleRepository.saveLesson(lesson);

        List<LessonStudent> lessonStudents = scheduleRepository.findLessonStudentsByLessonId(lessonId);

        for (LessonStudent ls : lessonStudents) {
            if ("PRESENT".equals(ls.getAttendanceStatus()) && !ls.getShouldConsume()) {
                BigDecimal consumeAmount = ls.getConsumeHours() != null
                        ? ls.getConsumeHours()
                        : lesson.getConsumeHours() != null
                            ? lesson.getConsumeHours()
                            : BigDecimal.ONE;

                StudentHourAccount account = hourApplicationService.consume(
                        ls.getStudentId(), consumeAmount, lessonId, ls.getId(),
                        "课程消耗 - " + lesson.getLessonNo(), currentUser);

                ls.setShouldConsume(true);
                ls.setConsumeHours(consumeAmount);
                ls.setHourTransactionId(account.getId());
                scheduleRepository.saveLessonStudent(ls);

                log.info("自动消课: studentId={}, lessonId={}, amount={}",
                        ls.getStudentId(), lessonId, consumeAmount);
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
        scheduleRepository.saveLesson(lesson);

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
