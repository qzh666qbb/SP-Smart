package com.example.training.schedule.application.service;

import com.example.training.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleScheduler {
    private static final Logger log = LoggerFactory.getLogger(ScheduleScheduler.class);


    private final ScheduleRepository scheduleRepository;
    private final ScheduleApplicationService scheduleApplicationService;

    @Scheduled(fixedRate = 300000)
    public void backfillMissingLessons() {
        scheduleApplicationService.backfillMissingLessons();
    }

    @Scheduled(fixedRate = 300000)
    public void markAbsentStudents() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        log.info("开始扫描旷课学员, 阈值时间: {}", threshold);

        int updated = scheduleRepository.markAbsentForCompletedLessons(threshold);
        log.info("旷课扫描完成, 更新了 {} 条记录", updated);
    }
}
