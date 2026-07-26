package com.example.training.student.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    
    private Long id;
    private String studentNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private Integer age;
    private String phone;
    private Long primaryTeacherId;
    private String primaryTeacherName;
    private LocalDate enrollmentDate;
    private String status;
    private String medicalNotes;
    private String remark;
    private LocalDateTime createdAt;
}
