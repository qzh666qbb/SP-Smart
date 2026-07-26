package com.example.training.physical.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
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
}
