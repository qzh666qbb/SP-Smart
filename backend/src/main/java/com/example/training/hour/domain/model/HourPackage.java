package com.example.training.hour.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourPackage {

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

    public boolean isEnabled() {
        return "ENABLED".equals(status);
    }
}
