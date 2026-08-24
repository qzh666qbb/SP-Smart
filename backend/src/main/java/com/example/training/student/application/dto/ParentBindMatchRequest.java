package com.example.training.student.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家长端"绑定孩子"的精确匹配请求。
 * 字段设计强制必须提供孩子学号 (studentNo) + 孩子姓名 + 家长手机号
 * 防止家长遍历孩子列表造成个人信息泄露。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentBindMatchRequest {

    @NotBlank(message = "孩子学号不能为空")
    @Size(max = 32, message = "学号长度不能超过32位")
    private String studentNo;

    @NotBlank(message = "孩子姓名不能为空")
    @Size(max = 64, message = "孩子姓名长度不能超过64位")
    private String studentName;

    @NotBlank(message = "家长手机号不能为空")
    @Size(max = 20, message = "家长手机号长度不能超过20位")
    private String parentPhone;

    public String getStudentNo() { return studentNo; }    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getParentPhone() { return parentPhone; }    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
}
