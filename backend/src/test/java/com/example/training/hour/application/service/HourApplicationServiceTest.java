package com.example.training.hour.application.service;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.dto.HourRechargeRequest;
import com.example.training.hour.application.dto.HourRefundRequest;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.hour.domain.model.StudentHourAccount;
import com.example.training.hour.domain.repository.HourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HourApplicationServiceTest {

    @Mock
    private HourRepository hourRepository;

    @InjectMocks
    private HourApplicationService hourApplicationService;

    private CurrentUser currentUser;

    @BeforeEach
    void setUp() {
        currentUser = CurrentUser.builder()
                .userId(1L)
                .username("admin")
                .role("ADMIN")
                .phone("13800000000")
                .build();
        ReflectionTestUtils.setField(hourApplicationService, "allowNegativeBalance", false);
    }

    private StudentHourAccount buildAccount(Long studentId, BigDecimal paid, BigDecimal gift,
                                            BigDecimal experience, BigDecimal activity, int version) {
        BigDecimal remaining = paid.add(gift).add(experience).add(activity);
        return StudentHourAccount.builder()
                .id(studentId * 100)
                .studentId(studentId)
                .totalRecharged(remaining)
                .totalConsumed(BigDecimal.ZERO)
                .totalRefunded(BigDecimal.ZERO)
                .remainingHours(remaining)
                .paidHours(paid)
                .giftHours(gift)
                .experienceHours(experience)
                .activityHours(activity)
                .version(version)
                .build();
    }

    @Nested
    @DisplayName("consume 消课方法测试")
    class ConsumeTests {

        @Test
        @DisplayName("余额充足 - 优先扣体验课时")
        void consume_EnoughBalance_ConsumeExperienceFirst() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("3"), BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(999L);
                return t;
            });

            HourTransaction tx = hourApplicationService.consume(1L, new BigDecimal("2"), 10L, 20L, "测试消耗", currentUser);

            assertNotNull(tx);
            assertEquals("CONSUME", tx.getTransactionType());
            assertEquals(new BigDecimal("-2"), tx.getChangeAmount());
            assertEquals(10L, tx.getLessonId());
            assertEquals(20L, tx.getLessonStudentId());
            assertEquals("EXPERIENCE", tx.getHourSource());
            verify(hourRepository).saveAccount(account);
            assertEquals(new BigDecimal("1"), account.getExperienceHours());
            assertEquals(new BigDecimal("11"), account.getRemainingHours());
        }

        @Test
        @DisplayName("余额充足 - 多来源依次扣减 (经验→活动→赠送→付费)")
        void consume_EnoughBalance_MultiSource() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, new BigDecimal("2"), BigDecimal.ONE, new BigDecimal("1"), 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(998L);
                return t;
            });

            HourTransaction tx = hourApplicationService.consume(1L, new BigDecimal("5"), 11L, 21L, null, currentUser);

            assertNotNull(tx);
            assertEquals(new BigDecimal("-5"), tx.getChangeAmount());
            assertEquals(BigDecimal.ZERO, account.getExperienceHours());
            assertEquals(BigDecimal.ZERO, account.getActivityHours());
            assertEquals(BigDecimal.ZERO, account.getGiftHours());
            assertEquals(new BigDecimal("9"), account.getPaidHours());
            assertEquals(new BigDecimal("9"), account.getRemainingHours());
            assertEquals(new BigDecimal("5"), account.getTotalConsumed());
        }

        @Test
        @DisplayName("余额不足 - 抛出 HOUR_INSUFFICIENT 异常")
        void consume_InsufficientBalance_ThrowsException() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.consume(1L, new BigDecimal("3"), 11L, 21L, "理由", currentUser));

            assertEquals(ErrorCode.HOUR_INSUFFICIENT, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("课时不足"));
            verify(hourRepository, never()).saveAccount(any());
            verify(hourRepository, never()).saveTransaction(any());
        }

        @Test
        @DisplayName("学员无课时账户 - 抛出 STUDENT_NOT_FOUND 异常")
        void consume_NoAccount_ThrowsStudentNotFound() {
            when(hourRepository.findAccountByStudentId(999L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.consume(999L, BigDecimal.ONE, 1L, 2L, "reason", currentUser));

            assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("没有课时账户"));
        }

        @Test
        @DisplayName("0 金额消耗 - 正常扣完返回 PAID 作为主来源")
        void consume_ZeroConsumeAmount_NoChangePaidUsed() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(900L);
                return t;
            });

            HourTransaction tx = hourApplicationService.consume(1L, BigDecimal.ZERO, 11L, 21L, null, currentUser);

            assertNotNull(tx);
            assertEquals(BigDecimal.ZERO, tx.getChangeAmount());
            assertEquals("PAID", tx.getHourSource());
            assertEquals(BigDecimal.TEN, account.getRemainingHours());
        }

        @Test
        @DisplayName("允许负余额 - 余额不足时继续扣入负数")
        void consume_AllowNegative_GoesNegative() {
            ReflectionTestUtils.setField(hourApplicationService, "allowNegativeBalance", true);
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(997L);
                return t;
            });

            HourTransaction tx = hourApplicationService.consume(1L, new BigDecimal("3"), 11L, 21L, "负数消费", currentUser);

            assertNotNull(tx);
            assertEquals(new BigDecimal("-3"), tx.getChangeAmount());
            assertEquals(new BigDecimal("-2"), account.getRemainingHours());
            assertEquals(new BigDecimal("-2"), account.getPaidHours());
        }

        @Test
        @DisplayName("乐观锁冲突 - saveAccount 抛出 MybatisPlusException 时转换为 BusinessException")
        void consume_OptimisticLock_ConvertsToBusinessException() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class)))
                    .thenThrow(new MybatisPlusException("乐观锁冲突"));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.consume(1L, new BigDecimal("2"), 1L, 2L, "reason", currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("并发操作冲突"));
            verify(hourRepository, never()).saveTransaction(any());
        }
    }

    @Nested
    @DisplayName("refund 退款方法测试")
    class RefundTests {

        @Test
        @DisplayName("退款成功 - 余额减少且流水生成")
        void refund_Success_BalanceReduced() {
            StudentHourAccount account = buildAccount(1L,
                    new BigDecimal("20"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(888L);
                return t;
            });

            HourRefundRequest request = HourRefundRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("5"))
                    .reason("测试退款")
                    .build();

            StudentHourAccount saved = hourApplicationService.refund(request, currentUser);

            assertNotNull(saved);
            assertEquals(new BigDecimal("15"), saved.getRemainingHours());
            assertEquals(new BigDecimal("15"), saved.getPaidHours());
            assertEquals(new BigDecimal("5"), saved.getTotalRefunded());
            verify(hourRepository).saveTransaction(argThat(tx ->
                    "REFUND".equals(tx.getTransactionType())
                            && new BigDecimal("-5").equals(tx.getChangeAmount())
                            && "测试退款".equals(tx.getReason())
                            && "PAID".equals(tx.getHourSource())
                            && 1L == tx.getStudentId()
            ));
        }

        @Test
        @DisplayName("退款理由为空 - 使用默认理由")
        void refund_NullReason_UseDefaultReason() {
            StudentHourAccount account = buildAccount(1L,
                    new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(887L);
                return t;
            });

            HourRefundRequest request = HourRefundRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("1"))
                    .build();

            hourApplicationService.refund(request, currentUser);

            verify(hourRepository).saveTransaction(argThat(tx ->
                    "课时退款".equals(tx.getReason())
            ));
        }

        @Test
        @DisplayName("退款导致负余额且不允许 - 抛出 HOUR_NEGATIVE_NOT_ALLOWED")
        void refund_ToNegativeDisallowed_ThrowsException() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));

            HourRefundRequest request = HourRefundRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("3"))
                    .reason("超额退款")
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.refund(request, currentUser));

            assertEquals(ErrorCode.HOUR_NEGATIVE_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("不允许负余额"));
            verify(hourRepository, never()).saveAccount(any());
            verify(hourRepository, never()).saveTransaction(any());
        }

        @Test
        @DisplayName("学员无课时账户 - 退款抛出 STUDENT_NOT_FOUND")
        void refund_NoAccount_ThrowsStudentNotFound() {
            when(hourRepository.findAccountByStudentId(999L)).thenReturn(Optional.empty());
            HourRefundRequest request = HourRefundRequest.builder()
                    .studentId(999L)
                    .amount(BigDecimal.ONE)
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.refund(request, currentUser));

            assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("refundByTransaction 按流水退回方法测试")
    class RefundByTransactionTests {

        @Test
        @DisplayName("正常退回 - EXPERIENCE 来源回到体验桶")
        void refundByTransaction_ExperienceSource_RefundedToExperienceBucket() {
            HourTransaction originalTx = HourTransaction.builder()
                    .id(500L)
                    .studentId(1L)
                    .transactionType("CONSUME")
                    .changeAmount(new BigDecimal("-2"))
                    .hourSource("EXPERIENCE")
                    .lessonId(10L)
                    .lessonStudentId(20L)
                    .build();
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

            when(hourRepository.findTransactionById(500L)).thenReturn(Optional.of(originalTx));
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(501L);
                return t;
            });

            StudentHourAccount result = hourApplicationService.refundByTransaction(500L, "手动退回", currentUser);

            assertNotNull(result);
            assertEquals(new BigDecimal("2"), account.getExperienceHours());
            assertEquals(new BigDecimal("12"), account.getRemainingHours());
            assertEquals(new BigDecimal("2"), account.getTotalRefunded());
            verify(hourRepository).saveTransaction(argThat(tx ->
                    "REFUND".equals(tx.getTransactionType())
                            && new BigDecimal("2").equals(tx.getChangeAmount())
                            && "EXPERIENCE".equals(tx.getHourSource())
                            && Long.valueOf(500L).equals(tx.getRelatedTransactionId())
            ));
            verify(hourRepository, times(2)).saveTransaction(any(HourTransaction.class));
        }

        @Test
        @DisplayName("非 CONSUME 流水 - 抛出异常")
        void refundByTransaction_NotConsume_ThrowsException() {
            HourTransaction rechargeTx = HourTransaction.builder()
                    .id(500L)
                    .studentId(1L)
                    .transactionType("RECHARGE")
                    .hourSource("PAID")
                    .build();
            when(hourRepository.findTransactionById(500L)).thenReturn(Optional.of(rechargeTx));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.refundByTransaction(500L, "reason", currentUser));

            assertEquals(ErrorCode.HOUR_TRANSACTION_FAILED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("只能对消耗流水"));
        }

        @Test
        @DisplayName("流水已被退回 - 抛出异常")
        void refundByTransaction_AlreadyRefunded_ThrowsException() {
            HourTransaction tx = HourTransaction.builder()
                    .id(500L)
                    .studentId(1L)
                    .transactionType("CONSUME")
                    .relatedTransactionId(999L)
                    .build();
            when(hourRepository.findTransactionById(500L)).thenReturn(Optional.of(tx));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.refundByTransaction(500L, "reason", currentUser));

            assertEquals(ErrorCode.HOUR_TRANSACTION_FAILED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("已被退回"));
        }

        @Test
        @DisplayName("原流水不存在 - 抛出异常")
        void refundByTransaction_NotFound_ThrowsException() {
            when(hourRepository.findTransactionById(anyLong())).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.refundByTransaction(9999L, "reason", currentUser));

            assertEquals(ErrorCode.HOUR_TRANSACTION_FAILED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("原消耗流水不存在"));
        }

        @Test
        @DisplayName("学员账户不存在 - 抛出异常")
        void refundByTransaction_NoAccount_ThrowsStudentNotFound() {
            HourTransaction originalTx = HourTransaction.builder()
                    .id(500L)
                    .studentId(1L)
                    .transactionType("CONSUME")
                    .changeAmount(new BigDecimal("-2"))
                    .build();
            when(hourRepository.findTransactionById(500L)).thenReturn(Optional.of(originalTx));
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.refundByTransaction(500L, "reason", currentUser));

            assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("乐观锁冲突 - saveAccount 抛出 MybatisPlusException")
        void refundByTransaction_OptimisticLock_ThrowsBusinessException() {
            HourTransaction originalTx = HourTransaction.builder()
                    .id(500L)
                    .studentId(1L)
                    .transactionType("CONSUME")
                    .changeAmount(new BigDecimal("-1"))
                    .hourSource("PAID")
                    .build();
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

            when(hourRepository.findTransactionById(500L)).thenReturn(Optional.of(originalTx));
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class)))
                    .thenThrow(new MybatisPlusException("version conflict"));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.refundByTransaction(500L, "reason", currentUser));

            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("并发操作冲突"));
        }

        @Test
        @DisplayName("小时来源为 null - 兜底使用 PAID")
        void refundByTransaction_NullSource_DefaultPaid() {
            HourTransaction originalTx = HourTransaction.builder()
                    .id(500L)
                    .studentId(1L)
                    .transactionType("CONSUME")
                    .changeAmount(new BigDecimal("-3"))
                    .hourSource(null)
                    .build();
            StudentHourAccount account = buildAccount(1L,
                    new BigDecimal("5"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

            when(hourRepository.findTransactionById(500L)).thenReturn(Optional.of(originalTx));
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(502L);
                return t;
            });

            hourApplicationService.refundByTransaction(500L, null, currentUser);

            assertEquals(new BigDecimal("8"), account.getPaidHours());
        }

        @Test
        @DisplayName("ACTIVITY 来源 - 退款进入活动课时桶，reason null 使用默认值")
        void refundByTransaction_ActivitySource_BackToActivityBucket() {
            HourTransaction originalTx = HourTransaction.builder()
                    .id(501L)
                    .studentId(1L)
                    .transactionType("CONSUME")
                    .changeAmount(new BigDecimal("-1"))
                    .hourSource("ACTIVITY")
                    .build();
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

            when(hourRepository.findTransactionById(501L)).thenReturn(Optional.of(originalTx));
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(510L);
                return t;
            });

            hourApplicationService.refundByTransaction(501L, null, currentUser);

            assertEquals(new BigDecimal("1"), account.getActivityHours());
            assertEquals(new BigDecimal("1"), account.getTotalRefunded());
            verify(hourRepository).saveTransaction(argThat(tx ->
                    "REFUND".equals(tx.getTransactionType())
                            && "课时退回".equals(tx.getReason())
            ));
        }

        @Test
        @DisplayName("GIFT 来源 - 退款进入赠送课时桶")
        void refundByTransaction_GiftSource_BackToGiftBucket() {
            HourTransaction originalTx = HourTransaction.builder()
                    .id(502L)
                    .studentId(1L)
                    .transactionType("CONSUME")
                    .changeAmount(new BigDecimal("-2"))
                    .hourSource("GIFT")
                    .build();
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

            when(hourRepository.findTransactionById(502L)).thenReturn(Optional.of(originalTx));
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(520L);
                return t;
            });

            hourApplicationService.refundByTransaction(502L, "赠课取消", currentUser);

            assertEquals(new BigDecimal("2"), account.getGiftHours());
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryTests {

        @Test
        @DisplayName("getAccount - 账户存在时返回")
        void getAccount_Found_ReturnsAccount() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));

            StudentHourAccount result = hourApplicationService.getAccount(1L);

            assertEquals(1L, result.getStudentId());
            assertEquals(BigDecimal.TEN, result.getRemainingHours());
        }

        @Test
        @DisplayName("getAccount - 不存在抛出 STUDENT_NOT_FOUND")
        void getAccount_NotFound_ThrowsException() {
            when(hourRepository.findAccountByStudentId(anyLong())).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    hourApplicationService.getAccount(999L));

            assertEquals(ErrorCode.STUDENT_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("getTransactions - 返回学员交易流水列表")
        void getTransactions_ReturnsList() {
            List<HourTransaction> list = List.of(
                    HourTransaction.builder().id(1L).studentId(1L).transactionType("RECHARGE").build(),
                    HourTransaction.builder().id(2L).studentId(1L).transactionType("CONSUME").build()
            );
            when(hourRepository.findTransactionsByStudentId(1L)).thenReturn(list);

            List<HourTransaction> result = hourApplicationService.getTransactions(1L);

            assertEquals(2, result.size());
            verify(hourRepository).findTransactionsByStudentId(1L);
        }
    }

    @Nested
    @DisplayName("recharge 充值方法测试")
    class RechargeTests {

        @Test
        @DisplayName("PAID 充值 - 学员已有账户，hourSource 为 null 走 PAID 分支")
        void recharge_PaidExistingAccount_DefaultToPaid() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(700L);
                return t;
            });

            HourRechargeRequest req = HourRechargeRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("5"))
                    .reason("购买课包")
                    .build();

            StudentHourAccount saved = hourApplicationService.recharge(req, currentUser, java.time.LocalDate.of(2027, 1, 1));

            assertEquals(new BigDecimal("15"), saved.getPaidHours());
            assertEquals(new BigDecimal("15"), saved.getRemainingHours());
            assertEquals(new BigDecimal("15"), saved.getTotalRecharged());
            verify(hourRepository).saveTransaction(argThat(tx ->
                    "RECHARGE".equals(tx.getTransactionType())
                            && new BigDecimal("5").equals(tx.getChangeAmount())
                            && "PAID".equals(tx.getHourSource())
                            && "购买课包".equals(tx.getReason())
                            && 1L == tx.getStudentId()
                            && tx.getExpireDate() != null
            ));
        }

        @Test
        @DisplayName("PAID 充值 - 学员尚无账户，自动创建")
        void recharge_PaidNewAccount_AutoCreatesAccount() {
            when(hourRepository.findAccountByStudentId(2L)).thenReturn(Optional.empty());
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> {
                StudentHourAccount a = inv.getArgument(0);
                if (a.getId() == null) a.setId(200L);
                return a;
            });
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(701L);
                return t;
            });

            HourRechargeRequest req = HourRechargeRequest.builder()
                    .studentId(2L)
                    .amount(new BigDecimal("10"))
                    .hourSource("PAID")
                    .build();

            StudentHourAccount saved = hourApplicationService.recharge(req, currentUser);

            assertNotNull(saved);
            assertEquals(new BigDecimal("10"), saved.getPaidHours());
            assertEquals(new BigDecimal("10"), saved.getRemainingHours());
            assertEquals(BigDecimal.ZERO, saved.getGiftHours());
            assertEquals(Integer.valueOf(0), saved.getVersion());
            verify(hourRepository, times(2)).saveAccount(any(StudentHourAccount.class));
        }

        @Test
        @DisplayName("GIFT 充值 - hourSource=GIFT 且非 PAID 时 expireDate 置空")
        void recharge_GiftSource_ExpireDateNulled() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ZERO, new BigDecimal("3"), BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(702L);
                return t;
            });

            HourRechargeRequest req = HourRechargeRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("2"))
                    .hourSource("GIFT")
                    .reason("赠送课时")
                    .build();

            StudentHourAccount saved = hourApplicationService.recharge(req, currentUser, java.time.LocalDate.of(2027, 6, 1));

            assertEquals(new BigDecimal("5"), saved.getGiftHours());
            assertEquals(new BigDecimal("5"), saved.getRemainingHours());
            verify(hourRepository).saveTransaction(argThat(tx ->
                    "GIFT".equals(tx.getHourSource()) && tx.getExpireDate() == null
            ));
        }

        @Test
        @DisplayName("EXPERIENCE 充值 - 进入体验课时桶")
        void recharge_ExperienceSource_ToExperienceBucket() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(703L);
                return t;
            });

            HourRechargeRequest req = HourRechargeRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("3"))
                    .hourSource("EXPERIENCE")
                    .build();

            StudentHourAccount saved = hourApplicationService.recharge(req, currentUser);

            assertEquals(new BigDecimal("4"), saved.getExperienceHours());
            assertEquals(new BigDecimal("4"), saved.getRemainingHours());
            verify(hourRepository).saveTransaction(argThat(tx -> "EXPERIENCE".equals(tx.getHourSource())));
        }

        @Test
        @DisplayName("ACTIVITY 充值 - 进入活动课时桶，reason 为 null 使用默认值")
        void recharge_ActivitySource_ActivityBucketDefaultReason() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(704L);
                return t;
            });

            HourRechargeRequest req = HourRechargeRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("1"))
                    .hourSource("ACTIVITY")
                    .build();

            StudentHourAccount saved = hourApplicationService.recharge(req, currentUser);

            assertEquals(new BigDecimal("2"), saved.getActivityHours());
            verify(hourRepository).saveTransaction(argThat(tx ->
                    "ACTIVITY".equals(tx.getHourSource()) && "课时充值".equals(tx.getReason())
            ));
        }

        @Test
        @DisplayName("未识别 hourSource - default 走 PAID 分支")
        void recharge_UnknownSource_DefaultsToPaid() {
            StudentHourAccount account = buildAccount(1L,
                    BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            when(hourRepository.findAccountByStudentId(1L)).thenReturn(Optional.of(account));
            when(hourRepository.saveAccount(any(StudentHourAccount.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hourRepository.saveTransaction(any(HourTransaction.class))).thenAnswer(inv -> {
                HourTransaction t = inv.getArgument(0);
                t.setId(705L);
                return t;
            });

            HourRechargeRequest req = HourRechargeRequest.builder()
                    .studentId(1L)
                    .amount(new BigDecimal("3"))
                    .hourSource("UNKNOWN_XXX")
                    .reason("不明来源")
                    .build();

            StudentHourAccount saved = hourApplicationService.recharge(req, currentUser);

            assertEquals(new BigDecimal("13"), saved.getPaidHours());
            verify(hourRepository).saveTransaction(argThat(tx -> "PAID".equals(tx.getHourSource())));
        }
    }

    @Nested
    @DisplayName("listAccounts 分页查询测试")
    class ListAccountsTests {

        @Test
        @DisplayName("正常查询 - 分页参数归一化，studentId 不为 null 计入结果")
        void listAccounts_Normal_ReturnsPage() {
            Object[] row1 = new Object[]{1L, 1L, "张三", "S001", "张父", "13800000001",
                    new BigDecimal("20"), new BigDecimal("5"), new BigDecimal("15"),
                    new BigDecimal("10"), new BigDecimal("5"), java.time.LocalDateTime.now()};
            Object[] row2 = new Object[]{2L, 2L, "李四", "S002", "李母", "13800000002",
                    new BigDecimal("10"), new BigDecimal("0"), new BigDecimal("10"),
                    new BigDecimal("10"), BigDecimal.ZERO, java.time.LocalDateTime.now()};
            List<Object[]> rows1 = new java.util.ArrayList<>();
            rows1.add(row1);
            rows1.add(row2);
            when(hourRepository.countAccounts("张")).thenReturn(2L);
            when(hourRepository.listAccountsRaw("张", 0, 10)).thenReturn(rows1);

            var page = hourApplicationService.listAccounts("张", 1, 10);

            assertEquals(2L, page.getTotal());
            assertEquals(1, page.getPageNum());
            assertEquals(2, page.getList().size());
            assertEquals("张三", page.getList().get(0).getStudentName());
            assertEquals(new BigDecimal("15"), page.getList().get(0).getRemainingHours());
        }

        @Test
        @DisplayName("分页参数边界 - pageNum<1 → 强制为1；pageSize>500 → 强制为500；studentId null 跳过")
        void listAccounts_ParamBounds_ForcedToSafeValues() {
            Object[] rowWithNullStudentId = new Object[]{1L, null, "幽灵", "S000", null, null,
                    new BigDecimal("10"), BigDecimal.ZERO, new BigDecimal("10"),
                    new BigDecimal("10"), BigDecimal.ZERO, java.time.LocalDateTime.now()};
            Object[] rowValid = new Object[]{2L, 2L, "李四", "S002", "李母", "13800000002",
                    new BigDecimal("10"), BigDecimal.ZERO, new BigDecimal("10"),
                    new BigDecimal("10"), BigDecimal.ZERO, java.time.LocalDateTime.now()};
            List<Object[]> rows2 = new java.util.ArrayList<>();
            rows2.add(rowWithNullStudentId);
            rows2.add(rowValid);
            when(hourRepository.countAccounts(null)).thenReturn(1L);
            when(hourRepository.listAccountsRaw(null, 0, 500)).thenReturn(rows2);

            var page = hourApplicationService.listAccounts(null, 0, 999);

            assertEquals(1, page.getList().size());
            assertEquals(2L, page.getList().get(0).getStudentId());
            assertEquals("李四", page.getList().get(0).getStudentName());
            verify(hourRepository).listAccountsRaw(null, 0, 500);
        }

        @Test
        @DisplayName("id 为 null - fallback studentId")
        void listAccounts_IdNull_FallbackStudentId() {
            Object[] rowIdNull = new Object[]{null, 3L, "王五", "S003", "王父", "13900000003",
                    new BigDecimal("30"), new BigDecimal("5"), new BigDecimal("25"),
                    new BigDecimal("20"), new BigDecimal("5"), java.time.LocalDateTime.now()};
            when(hourRepository.countAccounts("王")).thenReturn(1L);
            List<Object[]> rows = new java.util.ArrayList<>();
            rows.add(rowIdNull);
            when(hourRepository.listAccountsRaw("王", 50, 5)).thenReturn(rows);

            var page = hourApplicationService.listAccounts("王", 11, 5);

            assertEquals(1, page.getList().size());
            assertEquals(3L, page.getList().get(0).getId());
            assertEquals(3L, page.getList().get(0).getStudentId());
        }
    }
}
