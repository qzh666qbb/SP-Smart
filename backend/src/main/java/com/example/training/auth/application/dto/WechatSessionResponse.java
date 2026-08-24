package com.example.training.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WechatSessionResponse {

    private String openid;

    private String unionid;

    private String phone;

    private boolean needReview;

    private String pendingRole;

    private Long pendingApplyId;
}
