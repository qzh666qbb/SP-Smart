package com.example.training.student.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String parentName;
    private String parentPhone;
    private String parentRelation;
    private Long primaryTeacherId;
    private String primaryTeacherName;
    private LocalDate enrollmentDate;
    private String status;
    private String medicalNotes;
    private String remark;
    private LocalDateTime createdAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getStudentNo() { return studentNo; }    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getName() { return name; }    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthDate() { return birthDate; }    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public Integer getAge() { return age; }    public void setAge(Integer age) { this.age = age; }
    public String getPhone() { return phone; }    public void setPhone(String phone) { this.phone = phone; }
    public String getParentName() { return parentName; }    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentPhone() { return parentPhone; }    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public String getParentRelation() { return parentRelation; }    public void setParentRelation(String parentRelation) { this.parentRelation = parentRelation; }
    public Long getPrimaryTeacherId() { return primaryTeacherId; }    public void setPrimaryTeacherId(Long primaryTeacherId) { this.primaryTeacherId = primaryTeacherId; }
    public String getPrimaryTeacherName() { return primaryTeacherName; }    public void setPrimaryTeacherName(String primaryTeacherName) { this.primaryTeacherName = primaryTeacherName; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public String getMedicalNotes() { return medicalNotes; }    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
