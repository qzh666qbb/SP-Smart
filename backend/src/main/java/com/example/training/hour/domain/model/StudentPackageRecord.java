package com.example.training.hour.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPackageRecord {

    private Long id;
    private Long studentId;
    private Long packageId;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private BigDecimal totalHours;
    private BigDecimal remainingHours;
    private BigDecimal consumedHours;
    private BigDecimal refundedHours;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getPackageId() { return packageId; }    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public LocalDate getPurchaseDate() { return purchaseDate; }    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public LocalDate getExpireDate() { return expireDate; }    public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }
    public BigDecimal getTotalHours() { return totalHours; }    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public BigDecimal getRemainingHours() { return remainingHours; }    public void setRemainingHours(BigDecimal remainingHours) { this.remainingHours = remainingHours; }
    public BigDecimal getConsumedHours() { return consumedHours; }    public void setConsumedHours(BigDecimal consumedHours) { this.consumedHours = consumedHours; }
    public BigDecimal getRefundedHours() { return refundedHours; }    public void setRefundedHours(BigDecimal refundedHours) { this.refundedHours = refundedHours; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
