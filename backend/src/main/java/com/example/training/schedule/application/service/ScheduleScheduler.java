package com.example.training.schedule.application.service;

import com.example.training.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleScheduler {

    private final ScheduleRepository scheduleRepository;

    @Scheduled(fixedRate = 300000)
    public void markAbsentStudents() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        log.info("开始扫描旷课学员, 阈值时间: {}", threshold);

        int updated = scheduleRepository.markAbsentForCompletedLessons(threshold);
        log.info("旷课扫描完成, 更新了 {} 条记录", updated);
    }
}
