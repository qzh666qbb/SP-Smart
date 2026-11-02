package com.example.training.schedule.application.service;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.service.HourApplicationService;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleCompletionServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private HourApplicationService hourApplicationService;

    @InjectMocks
    private ScheduleCompletionService scheduleCompletionService;

    private CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        currentUser = CurrentUser.builder()
                .userId(100L)
                .username("teacher1")
                .role("TEACHER")
                .profileId(21L)
                .profileType("TEACHER")
                .build();
    }

    private Lesson buildLesson(Long id, String status, BigDecimal consumeHours) {
        return Lesson.builder()
                .id(id)
                .lessonNo("LSN" + id)
                .teacherId(21L)
                .consumeHours(consumeHours)
                .status(status)
                .version(0)
                .build();
    }

    private LessonStudent buildLessonStudent(Long id, Long lessonId, Long studentId, String attendanceStatus,
                                             Boolean shouldConsume, BigDecimal consumeHours, Long hourTransactionId) {
        return LessonStudent.builder()
                .id(id)
                .lessonId(lessonId)
                .studentId(studentId)
                .attendanceStatus(attendanceStatus)
                .shouldConsume(shouldConsume)
                .consumeHours(consumeHours)
                .hourTransactionId(hourTransactionId)
                .build();
    }

    @Nested
    @DisplayName("completeLessonWithAutoConsume 完成课程并自动消课")
    class CompleteLessonWithAutoConsumeTests {

        @Test
        @DisplayName("正常消课 - 状态 IN_PROGRESS、出席学员，使用 lessonStudent 指定的 consumeHours")
        void completeLesson_PendingPresent_LessonStudentHoursUsed() {
            Lesson lesson = buildLesson(1L, "IN_PROGRESS", new BigDecimal("3"));
            LessonStudent ls1 = buildLessonStudent(10L, 1L, 11L, "PRESENT", false, new BigDecimal("2"), null);
            List<LessonStudent> students = Collections.singletonList(ls1);

            when(scheduleRepository.findLessonById(1L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(1L)).thenReturn(students);
            HourTransaction mockTx = HourTransaction.builder().id(3000L).build();
            when(hourApplicationService.consume(eq(11L), eq(new BigDecimal("2")), eq(1L), eq(10L),
                    contains("课程消耗"), eq(currentUser))).thenReturn(mockTx);
            when(scheduleRepository.saveLessonStudent(any(LessonStudent.class))).thenAnswer(inv -> inv.getArgument(0));

            scheduleCompletionService.completeLessonWithAutoConsume(1L, currentUser);

            assertEquals("COMPLETED", lesson.getStatus());
            assertNotNull(lesson.getCompletedAt());
            assertEquals(100L, lesson.getCompletedBy());
            assertEquals(100L, lesson.getUpdatedBy());
            verify(hourApplicationService).consume(eq(11L), eq(new BigDecimal("2")), any(), any(), any(), any());
            verify(scheduleRepository).saveLessonStudent(argThat(ls ->
                    Boolean.TRUE.equals(ls.getShouldConsume())
                            && new BigDecimal("2").equals(ls.getConsumeHours())
                            && 3000L == ls.getHourTransactionId()
            ));
        }

        @Test
        @DisplayName("正常消课 - lessonStudent 的 consumeHours 为 null 时，从 lesson 取值")
        void completeLesson_LessonStudentHoursNull_UseLessonHours() {
            Lesson lesson = buildLesson(2L, "IN_PROGRESS", new BigDecimal("2"));
            LessonStudent ls = buildLessonStudent(20L, 2L, 22L, "PRESENT", false, null, null);
            when(scheduleRepository.findLessonById(2L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(2L)).thenReturn(Collections.singletonList(ls));
            HourTransaction mockTx = HourTransaction.builder().id(3001L).build();
            when(hourApplicationService.consume(eq(22L), eq(new BigDecimal("2")), any(), any(), any(), any())).thenReturn(mockTx);
            when(scheduleRepository.saveLessonStudent(any())).thenAnswer(inv -> inv.getArgument(0));

            scheduleCompletionService.completeLessonWithAutoConsume(2L, currentUser);

            verify(hourApplicationService).consume(anyLong(), eq(new BigDecimal("2")), any(), any(), any(), any());
        }

        @Test
        @DisplayName("正常消课 - lessonStudent 和 lesson 的 consumeHours 都为 null 时，默认 BigDecimal.ONE")
        void completeLesson_HoursAllNull_DefaultOne() {
            Lesson lesson = buildLesson(3L, "IN_PROGRESS", null);
            LessonStudent ls = buildLessonStudent(30L, 3L, 33L, "PRESENT", false, null, null);
            when(scheduleRepository.findLessonById(3L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(3L)).thenReturn(Collections.singletonList(ls));
            HourTransaction mockTx = HourTransaction.builder().id(3002L).build();
            when(hourApplicationService.consume(eq(33L), eq(BigDecimal.ONE), any(), any(), any(), any())).thenReturn(mockTx);
            when(scheduleRepository.saveLessonStudent(any())).thenAnswer(inv -> inv.getArgument(0));

            scheduleCompletionService.completeLessonWithAutoConsume(3L, currentUser);

            verify(hourApplicationService).consume(anyLong(), eq(BigDecimal.ONE), any(), any(), any(), any());
        }

        @Test
        @DisplayName("0 课时跳过 - consumeAmount 为 0 时不调用消课")
        void completeLesson_ZeroConsumeHours_SkipConsume() {
            Lesson lesson = buildLesson(4L, "IN_PROGRESS", BigDecimal.ZERO);
            LessonStudent ls = buildLessonStudent(40L, 4L, 44L, "PRESENT", false, null, null);
            when(scheduleRepository.findLessonById(4L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(4L)).thenReturn(Collections.singletonList(ls));

            scheduleCompletionService.completeLessonWithAutoConsume(4L, currentUser);

            verify(hourApplicationService, never()).consume(any(), any(), any(), any(), any(), any());
            verify(scheduleRepository, never()).saveLessonStudent(any());
        }

        @Test
        @DisplayName("重复消课拦截 - 课程已 COMPLETED")
        void completeLesson_AlreadyCompleted_ThrowsException() {
            Lesson lesson = buildLesson(5L, "COMPLETED", new BigDecimal("2"));
            when(scheduleRepository.findLessonById(5L)).thenReturn(Optional.of(lesson));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.completeLessonWithAutoConsume(5L, currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("进行中"));
            verify(scheduleRepository, never()).saveLesson(any());
            verify(hourApplicationService, never()).consume(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("重复消课拦截 - 课程已 CANCELLED")
        void completeLesson_AlreadyCancelled_ThrowsException() {
            Lesson lesson = buildLesson(6L, "CANCELLED", new BigDecimal("2"));
            when(scheduleRepository.findLessonById(6L)).thenReturn(Optional.of(lesson));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.completeLessonWithAutoConsume(6L, currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("进行中"));
        }

        @Test
        @DisplayName("未开始课程不能直接结课")
        void completeLesson_Pending_ThrowsException() {
            Lesson lesson = buildLesson(17L, "PENDING", BigDecimal.ONE);
            when(scheduleRepository.findLessonById(17L)).thenReturn(Optional.of(lesson));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.completeLessonWithAutoConsume(17L, currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("进行中"));
            verify(scheduleRepository, never()).saveLesson(any());
        }

        @Test
        @DisplayName("shouldConsume 为空时按未消课处理")
        void completeLesson_NullShouldConsume_Consumes() {
            Lesson lesson = buildLesson(18L, "IN_PROGRESS", BigDecimal.ONE);
            LessonStudent student = buildLessonStudent(180L, 18L, 181L, "PRESENT", null, null, null);
            when(scheduleRepository.findLessonById(18L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any())).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(18L)).thenReturn(List.of(student));
            when(hourApplicationService.consume(anyLong(), any(), anyLong(), anyLong(), any(), any()))
                    .thenReturn(HourTransaction.builder().id(1800L).build());
            when(scheduleRepository.saveLessonStudent(any())).thenAnswer(inv -> inv.getArgument(0));

            scheduleCompletionService.completeLessonWithAutoConsume(18L, currentUser);

            verify(hourApplicationService).consume(eq(181L), eq(BigDecimal.ONE), eq(18L), eq(180L), any(), eq(currentUser));
        }

        @Test
        @DisplayName("迟到学员正常消课")
        void completeLesson_Late_Consumes() {
            Lesson lesson = buildLesson(20L, "IN_PROGRESS", BigDecimal.ONE);
            LessonStudent student = buildLessonStudent(200L, 20L, 201L, "LATE", false, null, null);
            when(scheduleRepository.findLessonById(20L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any())).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(20L)).thenReturn(List.of(student));
            when(hourApplicationService.consume(anyLong(), any(), anyLong(), anyLong(), any(), any()))
                    .thenReturn(HourTransaction.builder().id(2000L).build());
            when(scheduleRepository.saveLessonStudent(any())).thenAnswer(inv -> inv.getArgument(0));

            scheduleCompletionService.completeLessonWithAutoConsume(20L, currentUser);

            verify(hourApplicationService).consume(eq(201L), eq(BigDecimal.ONE), eq(20L), eq(200L), any(), eq(currentUser));
        }

        @Test
        @DisplayName("已消课学员跳过 - shouldConsume=true 不重复处理")
        void completeLesson_ShouldConsumeTrue_Skip() {
            Lesson lesson = buildLesson(7L, "IN_PROGRESS", new BigDecimal("2"));
            LessonStudent ls = buildLessonStudent(70L, 7L, 77L, "PRESENT", true, new BigDecimal("2"), 777L);
            when(scheduleRepository.findLessonById(7L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(7L)).thenReturn(Collections.singletonList(ls));

            scheduleCompletionService.completeLessonWithAutoConsume(7L, currentUser);

            verify(hourApplicationService, never()).consume(any(), any(), any(), any(), any(), any());
            verify(scheduleRepository, never()).saveLessonStudent(any());
        }

        @Test
        @DisplayName("非出席学员跳过 - 非 PRESENT 不处理")
        void completeLesson_NotPresent_StatusSkipped() {
            Lesson lesson = buildLesson(8L, "IN_PROGRESS", new BigDecimal("2"));
            LessonStudent ls = buildLessonStudent(80L, 8L, 88L, "ABSENT", false, null, null);
            when(scheduleRepository.findLessonById(8L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(8L)).thenReturn(Collections.singletonList(ls));

            scheduleCompletionService.completeLessonWithAutoConsume(8L, currentUser);

            verify(hourApplicationService, never()).consume(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("课程不存在 - 抛出 LESSON_NOT_FOUND")
        void completeLesson_LessonNotFound_ThrowsException() {
            when(scheduleRepository.findLessonById(anyLong())).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.completeLessonWithAutoConsume(9999L, currentUser));

            assertEquals(ErrorCode.LESSON_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("课程归属校验 - 教师不能完成其他教师的课程")
        void completeLesson_OtherTeacher_ThrowsException() {
            Lesson lesson = buildLesson(16L, "PENDING", BigDecimal.ONE);
            lesson.setTeacherId(999L);
            when(scheduleRepository.findLessonById(16L)).thenReturn(Optional.of(lesson));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.completeLessonWithAutoConsume(16L, currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertEquals("只能操作本人负责的课程", ex.getMessage());
            verify(scheduleRepository, never()).saveLesson(any());
        }

        @Test
        @DisplayName("乐观锁冲突 - saveLesson 抛出 MybatisPlusException 时转换")
        void completeLesson_OptimisticLock_ConvertsToBusinessException() {
            Lesson lesson = buildLesson(9L, "IN_PROGRESS", new BigDecimal("2"));
            when(scheduleRepository.findLessonById(9L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class)))
                    .thenThrow(new MybatisPlusException("乐观锁冲突"));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.completeLessonWithAutoConsume(9L, currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("不能重复完成"));
            verify(hourApplicationService, never()).consume(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("DataAccessException - saveLesson 抛出数据访问异常")
        void completeLesson_DataAccessException_ConvertsToBusinessException() {
            Lesson lesson = buildLesson(10L, "IN_PROGRESS", new BigDecimal("2"));
            when(scheduleRepository.findLessonById(10L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class)))
                    .thenThrow(mock(DataAccessException.class));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.completeLessonWithAutoConsume(10L, currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("状态已变更"));
        }
    }

    @Nested
    @DisplayName("cancelLessonWithRefund 取消课程并退回课时")
    class CancelLessonWithRefundTests {

        @Test
        @DisplayName("正常取消并退回 - 已消课学员触发退回")
        void cancelLesson_WithConsumedStudent_RefundByTransaction() {
            Lesson lesson = buildLesson(11L, "COMPLETED", new BigDecimal("2"));
            LessonStudent consumed = buildLessonStudent(100L, 11L, 111L, "PRESENT", true, new BigDecimal("2"), 9000L);
            LessonStudent notConsumed = buildLessonStudent(101L, 11L, 112L, "PRESENT", false, null, null);
            when(scheduleRepository.findLessonById(11L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(11L)).thenReturn(List.of(consumed, notConsumed));

            CurrentUser admin = CurrentUser.builder()
                    .userId(1L).username("admin").role("ADMIN").build();

            scheduleCompletionService.cancelLessonWithRefund(11L, "临时调课", admin);

            assertEquals("CANCELLED", lesson.getStatus());
            assertEquals("临时调课", lesson.getCancelReason());
            assertEquals(1L, lesson.getUpdatedBy());
            verify(hourApplicationService).refundByTransaction(eq(9000L), contains("课程取消退回"), eq(admin));
            verify(hourApplicationService, never()).refundByTransaction(eq(-1L), any(), any());
        }

        @Test
        @DisplayName("取消 - 没有已消课学员，不调用 refundByTransaction")
        void cancelLesson_NoConsumedStudents_NoRefund() {
            Lesson lesson = buildLesson(12L, "PENDING", new BigDecimal("2"));
            LessonStudent ls = buildLessonStudent(120L, 12L, 121L, "PRESENT", false, null, null);
            when(scheduleRepository.findLessonById(12L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));
            when(scheduleRepository.findLessonStudentsByLessonId(12L)).thenReturn(Collections.singletonList(ls));

            scheduleCompletionService.cancelLessonWithRefund(12L, null, currentUser);

            verify(hourApplicationService, never()).refundByTransaction(anyLong(), any(), any());
        }

        @Test
        @DisplayName("重复取消拦截 - 课程已 CANCELLED")
        void cancelLesson_AlreadyCancelled_ThrowsException() {
            Lesson lesson = buildLesson(13L, "CANCELLED", new BigDecimal("2"));
            when(scheduleRepository.findLessonById(13L)).thenReturn(Optional.of(lesson));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.cancelLessonWithRefund(13L, "reason", currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("已取消"));
            verify(scheduleRepository, never()).saveLesson(any());
        }

        @Test
        @DisplayName("教师不能取消已完成课程")
        void cancelLesson_CompletedByTeacher_ThrowsException() {
            Lesson lesson = buildLesson(19L, "COMPLETED", BigDecimal.ONE);
            when(scheduleRepository.findLessonById(19L)).thenReturn(Optional.of(lesson));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.cancelLessonWithRefund(19L, "误操作", currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("管理员"));
            verify(scheduleRepository, never()).saveLesson(any());
        }

        @Test
        @DisplayName("课程不存在 - 抛出 LESSON_NOT_FOUND")
        void cancelLesson_LessonNotFound_ThrowsException() {
            when(scheduleRepository.findLessonById(anyLong())).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.cancelLessonWithRefund(9999L, "reason", currentUser));

            assertEquals(ErrorCode.LESSON_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("乐观锁冲突 - saveLesson 抛出 MybatisPlusException")
        void cancelLesson_OptimisticLock_ThrowsBusinessException() {
            Lesson lesson = buildLesson(14L, "PENDING", new BigDecimal("2"));
            when(scheduleRepository.findLessonById(14L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class)))
                    .thenThrow(new MybatisPlusException("version conflict"));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.cancelLessonWithRefund(14L, "reason", currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("课程已取消，不能重复操作"));
            verify(hourApplicationService, never()).refundByTransaction(anyLong(), any(), any());
        }

        @Test
        @DisplayName("DataAccessException - saveLesson 抛出数据访问异常")
        void cancelLesson_DataAccessException_Converts() {
            Lesson lesson = buildLesson(15L, "PENDING", new BigDecimal("2"));
            when(scheduleRepository.findLessonById(15L)).thenReturn(Optional.of(lesson));
            when(scheduleRepository.saveLesson(any(Lesson.class)))
                    .thenThrow(mock(DataAccessException.class));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    scheduleCompletionService.cancelLessonWithRefund(15L, "reason", currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("状态已变更"));
        }
    }
}
