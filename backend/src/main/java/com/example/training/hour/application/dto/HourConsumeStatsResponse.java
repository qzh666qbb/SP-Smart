package com.example.training.hour.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class HourConsumeStatsResponse {
    private BigDecimal totalHours;
    private int totalCount;
    private Map<String, BigDecimal> byCourseType;
    private Map<String, BigDecimal> byHourSource;
}
