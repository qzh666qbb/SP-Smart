package com.example.training.hour.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("student_hour_account")
public class StudentHourAccountEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    private BigDecimal totalRecharged;
    private BigDecimal totalConsumed;
    private BigDecimal totalRefunded;
    private BigDecimal remainingHours;
    
    @TableField("paid_hours")
    private BigDecimal paidHours;
    
    @TableField("gift_hours")
    private BigDecimal giftHours;
    
    @TableField("experience_hours")
    private BigDecimal experienceHours;
    
    @TableField("activity_hours")
    private BigDecimal activityHours;
    
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
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
