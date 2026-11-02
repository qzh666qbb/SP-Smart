package com.example.training.auth.domain.service;

import com.example.training.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.ArgumentCaptor;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LegacyWechatTicketServiceTest {
    @Test void exchangesVerifiedCodeForExpiringSingleUseTicket() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String,String> values = mock(ValueOperations.class);
        WechatGateway gateway = mock(WechatGateway.class);
        when(redis.opsForValue()).thenReturn(values);
        var identity = new WechatGateway.WechatIdentity("verified-openid", "union");
        when(gateway.exchangeCode("code")).thenReturn(identity);
        var service = new LegacyWechatTicketService(redis, new ObjectMapper(), gateway);
        String ticket = service.exchange("code");
        assertTrue(ticket.matches("compat_[a-f0-9]{32}"));
        assertNotEquals(identity.openid(), ticket);
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(values).set(eq("wechat:compat:"+ticket), json.capture(), eq(Duration.ofMinutes(2)));
        when(values.getAndDelete("wechat:compat:"+ticket)).thenReturn(json.getValue(), null);
        assertEquals(identity, service.consume(ticket));
        assertThrows(BusinessException.class, () -> service.consume(ticket));
        assertThrows(BusinessException.class, () -> service.consume("verified-openid"));
    }
}
