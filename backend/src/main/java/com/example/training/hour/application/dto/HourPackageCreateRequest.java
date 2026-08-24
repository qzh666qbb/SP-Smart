package com.example.training.hour.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourPackageCreateRequest {

    private String packageName;
    private String packageType;
    private BigDecimal totalHours;
    private BigDecimal price;
    private Integer validMonths;
    private String description;

    public String getPackageName() { return packageName; }    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getPackageType() { return packageType; }    public void setPackageType(String packageType) { this.packageType = packageType; }
    public BigDecimal getTotalHours() { return totalHours; }    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public BigDecimal getPrice() { return price; }    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getValidMonths() { return validMonths; }    public void setValidMonths(Integer validMonths) { this.validMonths = validMonths; }
    public String getDescription() { return description; }    public void setDescription(String description) { this.description = description; }
}
