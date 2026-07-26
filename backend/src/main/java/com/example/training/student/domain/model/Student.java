package com.example.training.student.domain.model;

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
public class Student {
    
    private Long id;
    private String studentNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private Long primaryTeacherId;
    private LocalDate enrollmentDate;
    private String status;
    private String medicalNotes;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return LocalDate.now().getYear() - birthDate.getYear();
    }
}
