package com.example.training.physical.infrastructure.persistence;

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
@TableName("physical_score_detail")
public class PhysicalScoreDetailEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long recordId;
    private Long itemId;
    private BigDecimal rawValue;
    private BigDecimal convertedScore;
    private String level;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getItemId() { return itemId; }    public void setItemId(Long itemId) { this.itemId = itemId; }
    public BigDecimal getRawValue() { return rawValue; }    public void setRawValue(BigDecimal rawValue) { this.rawValue = rawValue; }
    public BigDecimal getConvertedScore() { return convertedScore; }    public void setConvertedScore(BigDecimal convertedScore) { this.convertedScore = convertedScore; }
    public String getLevel() { return level; }    public void setLevel(String level) { this.level = level; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
