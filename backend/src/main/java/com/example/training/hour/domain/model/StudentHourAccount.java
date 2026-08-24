package com.example.training.hour.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentHourAccount {
    
    private Long id;
    private Long studentId;
    private BigDecimal totalRecharged;
    private BigDecimal totalConsumed;
    private BigDecimal totalRefunded;
    private BigDecimal remainingHours;
    private BigDecimal paidHours;
    private BigDecimal giftHours;
    private BigDecimal experienceHours;
    private BigDecimal activityHours;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public BigDecimal getTotalRecharged() { return totalRecharged; }    public void setTotalRecharged(BigDecimal totalRecharged) { this.totalRecharged = totalRecharged; }
    public BigDecimal getTotalConsumed() { return totalConsumed; }    public void setTotalConsumed(BigDecimal totalConsumed) { this.totalConsumed = totalConsumed; }
    public BigDecimal getTotalRefunded() { return totalRefunded; }    public void setTotalRefunded(BigDecimal totalRefunded) { this.totalRefunded = totalRefunded; }
    public BigDecimal getRemainingHours() { return remainingHours; }    public void setRemainingHours(BigDecimal remainingHours) { this.remainingHours = remainingHours; }
    public BigDecimal getPaidHours() { return paidHours; }    public void setPaidHours(BigDecimal paidHours) { this.paidHours = paidHours; }
    public BigDecimal getGiftHours() { return giftHours; }    public void setGiftHours(BigDecimal giftHours) { this.giftHours = giftHours; }
    public BigDecimal getExperienceHours() { return experienceHours; }    public void setExperienceHours(BigDecimal experienceHours) { this.experienceHours = experienceHours; }
    public BigDecimal getActivityHours() { return activityHours; }    public void setActivityHours(BigDecimal activityHours) { this.activityHours = activityHours; }
    public Integer getVersion() { return version; }    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
