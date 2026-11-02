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
public class PhysicalScoreDetail {
    
    private Long id;
    private Long recordId;
    private Long itemId;
    private String itemName;
    private String unit;
    private BigDecimal rawValue;
    private BigDecimal convertedScore;
    private String level;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getItemId() { return itemId; }    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getUnit() { return unit; }    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getRawValue() { return rawValue; }    public void setRawValue(BigDecimal rawValue) { this.rawValue = rawValue; }
    public BigDecimal getConvertedScore() { return convertedScore; }    public void setConvertedScore(BigDecimal convertedScore) { this.convertedScore = convertedScore; }
    public String getLevel() { return level; }    public void setLevel(String level) { this.level = level; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
