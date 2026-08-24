package com.example.training.physical.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTestTemplate {
    
    private Long id;
    private String templateName;
    private String templateCode;
    private String targetGender;
    private Integer minAge;
    private Integer maxAge;
    private String description;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getTemplateName() { return templateName; }    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateCode() { return templateCode; }    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTargetGender() { return targetGender; }    public void setTargetGender(String targetGender) { this.targetGender = targetGender; }
    public Integer getMinAge() { return minAge; }    public void setMinAge(Integer minAge) { this.minAge = minAge; }
    public Integer getMaxAge() { return maxAge; }    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }
    public String getDescription() { return description; }    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
