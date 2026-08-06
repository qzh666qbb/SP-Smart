package com.example.training.hour.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourPackageUpdateRequest {

    private String packageName;
    private String packageType;
    private BigDecimal totalHours;
    private BigDecimal price;
    private Integer validMonths;
    private String description;
}
