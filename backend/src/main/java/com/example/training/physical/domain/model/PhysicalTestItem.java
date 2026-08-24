package com.example.training.physical.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTestItem {
    
    private Long id;
    private String itemCode;
    private String itemName;
    private String itemType;
    private String unit;
    private Boolean genderSpecific;
    private Integer ageMin;
    private Integer ageMax;
    private String description;
    private String status;
    private Integer sortOrder;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isEnabled() {
        return "ENABLED".equals(status);
    }

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getItemCode() { return itemCode; }    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemType() { return itemType; }    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getUnit() { return unit; }    public void setUnit(String unit) { this.unit = unit; }
    public Boolean getGenderSpecific() { return genderSpecific; }    public void setGenderSpecific(Boolean genderSpecific) { this.genderSpecific = genderSpecific; }
    public Integer getAgeMin() { return ageMin; }    public void setAgeMin(Integer ageMin) { this.ageMin = ageMin; }
    public Integer getAgeMax() { return ageMax; }    public void setAgeMax(Integer ageMax) { this.ageMax = ageMax; }
    public String getDescription() { return description; }    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
