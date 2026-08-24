package com.example.training.hour.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

public class HourConsumeStatsResponse {
    private BigDecimal totalHours;
    private int totalCount;
    private Map<String, BigDecimal> byCourseType;
    private Map<String, BigDecimal> byHourSource;

    public BigDecimal getTotalHours() { return totalHours; }    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public int getTotalCount() { return totalCount; }    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public Map<String, BigDecimal> getByCourseType() { return byCourseType; }    public void setByCourseType(Map<String, BigDecimal> byCourseType) { this.byCourseType = byCourseType; }
    public Map<String, BigDecimal> getByHourSource() { return byHourSource; }    public void setByHourSource(Map<String, BigDecimal> byHourSource) { this.byHourSource = byHourSource; }
}
