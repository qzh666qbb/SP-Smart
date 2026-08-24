package com.example.training.teacher.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeacherApplyReviewRequest {

    private Long applyId;

    @NotNull(message = "审批结果不能为空")
    private Boolean approve;

    @Size(max = 200, message = "审核备注最多200字符")
    private String comment;
}
