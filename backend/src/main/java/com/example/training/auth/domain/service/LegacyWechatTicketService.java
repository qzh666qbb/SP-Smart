package com.example.training.auth.domain.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.UUID;

/** 旧客户端将 openid 原样传回；该字段只承载一次性随机凭证，不接受真实 openid。 */
@Service
@RequiredArgsConstructor
public class LegacyWechatTicketService {
    public static final String PREFIX = "compat_";
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final WechatGateway gateway;

    public String exchange(String code) {
        var identity = gateway.exchangeCode(code);
        String ticket = PREFIX + UUID.randomUUID().toString().replace("-", "");
        try {
            redis.opsForValue().set("wechat:compat:" + ticket, mapper.writeValueAsString(identity), Duration.ofMinutes(2));
            return ticket;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED, "登录暂时不可用，请重试");
        }
    }

    public WechatGateway.WechatIdentity consume(String ticket) {
        if (ticket == null || !ticket.matches("compat_[a-f0-9]{32}")) throw invalid();
        String value = redis.opsForValue().getAndDelete("wechat:compat:" + ticket);
        if (value == null) throw invalid();
        try { return mapper.readValue(value, WechatGateway.WechatIdentity.class); }
        catch (Exception e) { throw invalid(); }
    }

    private BusinessException invalid() {
        return new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "微信登录凭证已失效，请重新登录");
    }
}
