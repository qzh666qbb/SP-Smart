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
public class StudentCreateRequest {
    
    private String studentNo;
    
    @NotBlank(message = "学员姓名不能为空")
    private String name;
    
    @Pattern(regexp = "^(MALE|FEMALE|UNKNOWN)$", message = "性别必须是MALE、FEMALE或UNKNOWN")
    private String gender;
    
    private LocalDate birthDate;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String parentName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "家长手机号格式不正确")
    private String parentPhone;

    @Pattern(regexp = "^(FATHER|MOTHER|GUARDIAN|OTHER)$", message = "家长关系类型必须是FATHER、MOTHER、GUARDIAN或OTHER")
    private String parentRelation;

    private Long primaryTeacherId;
    
    private LocalDate enrollmentDate;
    
    private String medicalNotes;
    
    private String remark;

    public String getStudentNo() { return studentNo; }    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getName() { return name; }    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthDate() { return birthDate; }    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhone() { return phone; }    public void setPhone(String phone) { this.phone = phone; }
    public String getParentName() { return parentName; }    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentPhone() { return parentPhone; }    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public String getParentRelation() { return parentRelation; }    public void setParentRelation(String parentRelation) { this.parentRelation = parentRelation; }
    public Long getPrimaryTeacherId() { return primaryTeacherId; }    public void setPrimaryTeacherId(Long primaryTeacherId) { this.primaryTeacherId = primaryTeacherId; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public String getMedicalNotes() { return medicalNotes; }    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
}
