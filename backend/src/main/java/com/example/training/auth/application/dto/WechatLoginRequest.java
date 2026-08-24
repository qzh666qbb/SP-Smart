package com.example.training.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginRequest {
    
    @NotBlank(message = "openid不能为空")
    private String openid;
    
    private String unionid;

    public String getOpenid() { return openid; }    public void setOpenid(String openid) { this.openid = openid; }
    public String getUnionid() { return unionid; }    public void setUnionid(String unionid) { this.unionid = unionid; }
}
