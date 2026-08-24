package com.example.training.student.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChildCreateRequest {

    @NotBlank(message = "孩子姓名不能为空")
    private String name;

    @Pattern(regexp = "^(MALE|FEMALE|UNKNOWN)$", message = "性别必须是MALE、FEMALE或UNKNOWN")
    private String gender;

    private LocalDate birthDate;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String medicalNotes;

    private String remark;
}
