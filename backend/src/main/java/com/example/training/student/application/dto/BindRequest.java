package com.example.training.student.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindRequest {
    
    @NotNull(message = "学员ID不能为空")
    private Long studentId;
    
    @Pattern(regexp = "^(FATHER|MOTHER|GUARDIAN|OTHER)$", message = "关系类型必须是FATHER、MOTHER、GUARDIAN或OTHER")
    private String relationType;
}
