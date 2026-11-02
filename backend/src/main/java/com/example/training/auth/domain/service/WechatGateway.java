package com.example.training.auth.domain.service;

public interface WechatGateway {
    WechatIdentity exchangeCode(String code);

    record WechatIdentity(String openid, String unionid) {}
}
