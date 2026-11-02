package com.example.training.physical.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PhysicalTestBatchCreateRequest {
    @NotBlank(message = "批次名称不能为空")
    @Size(max = 100, message = "批次名称不能超过100个字符")
    private String batchName;

    @NotNull(message = "测试日期不能为空")
    private LocalDate testDate;

    @Size(max = 255, message = "测试地点不能超过255个字符")
    private String location;
}
