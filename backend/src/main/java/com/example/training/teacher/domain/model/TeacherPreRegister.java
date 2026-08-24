package com.example.training.teacher.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeacherPreRegister {

    private Long id;
    private String phone;
    private String realName;
    private Long createdBy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
