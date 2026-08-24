package com.example.training.hour.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hour_package")
public class HourPackageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String packageCode;
    private String packageName;
    private String packageType;
    private BigDecimal totalHours;
    private BigDecimal price;
    private Integer validMonths;
    private String description;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getPackageCode() { return packageCode; }    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getPackageName() { return packageName; }    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getPackageType() { return packageType; }    public void setPackageType(String packageType) { this.packageType = packageType; }
    public BigDecimal getTotalHours() { return totalHours; }    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public BigDecimal getPrice() { return price; }    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getValidMonths() { return validMonths; }    public void setValidMonths(Integer validMonths) { this.validMonths = validMonths; }
    public String getDescription() { return description; }    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
