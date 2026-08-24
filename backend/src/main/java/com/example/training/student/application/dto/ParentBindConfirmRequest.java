package com.example.training.student.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家长端绑定确认请求：家长通过精确匹配找到孩子后，
 * 用 matchToken 或直接用 studentId+relationType 发起绑定（PENDING）。
 * 这里采用 studentId + 关系类型（直接绑定），与前端匹配。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentBindConfirmRequest {

    @NotNull(message = "学员ID不能为空")
    private Long studentId;

    @Pattern(regexp = "^(FATHER|MOTHER|GUARDIAN|OTHER)$", message = "关系类型必须是 FATHER、MOTHER、GUARDIAN 或 OTHER")
    private String relationType;

    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getRelationType() { return relationType; }    public void setRelationType(String relationType) { this.relationType = relationType; }
}
