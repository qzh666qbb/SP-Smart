package com.example.training.auth.infrastructure.wechat;

import com.example.training.auth.domain.service.WechatGateway;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class WechatGatewayImpl implements WechatGateway {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.wechat.appid:}")
    private String appid;

    @Value("${app.wechat.secret:}")
    private String secret;

    @Value("${app.wechat.mock-openid-when-disabled:false}")
    private boolean mockWhenDisabled;

    @Override
    public WechatIdentity exchangeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
        }
        boolean disabled = appid == null || appid.isBlank() || secret == null || secret.isBlank();
        if (disabled) {
            if (!mockWhenDisabled || !code.startsWith("mock_")) {
                throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
            }
            String value = code.substring(5).replaceAll("[^A-Za-z0-9_-]", "");
            if (value.isBlank()) {
                throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
            }
            return new WechatIdentity("ox_" + value, "ux_" + value);
        }

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                    .queryParam("appid", appid)
                    .queryParam("secret", secret)
                    .queryParam("js_code", code)
                    .queryParam("grant_type", "authorization_code")
                    .build().encode().toUriString();
            JsonNode node = objectMapper.readTree(restTemplate.getForObject(url, String.class));
            if ((node.has("errcode") && node.get("errcode").asInt() != 0)
                    || !node.hasNonNull("openid") || node.get("openid").asText().isBlank()) {
                throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
            }
            String unionid = node.hasNonNull("unionid") ? node.get("unionid").asText() : null;
            return new WechatIdentity(node.get("openid").asText(), unionid);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("微信code2session失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
        }
    }
}
