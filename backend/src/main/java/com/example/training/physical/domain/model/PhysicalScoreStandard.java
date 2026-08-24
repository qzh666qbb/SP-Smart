package com.example.training.physical.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalScoreStandard {

    private Long id;
    private Long itemId;
    private String gender;
    private String ageGroup;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer score;
    private String level;
    private BigDecimal scoreValue;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getItemId() { return itemId; }    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getGender() { return gender; }    public void setGender(String gender) { this.gender = gender; }
    public String getAgeGroup() { return ageGroup; }    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    public BigDecimal getMinValue() { return minValue; }    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
    public BigDecimal getMaxValue() { return maxValue; }    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    public Integer getScore() { return score; }    public void setScore(Integer score) { this.score = score; }
    public String getLevel() { return level; }    public void setLevel(String level) { this.level = level; }
    public BigDecimal getScoreValue() { return scoreValue; }    public void setScoreValue(BigDecimal scoreValue) { this.scoreValue = scoreValue; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
