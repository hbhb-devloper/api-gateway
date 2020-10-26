package com.hbhb.cw.gateway.component;

import com.hbhb.cw.gateway.enums.AuthErrorCode;
import com.hbhb.cw.gateway.util.ResponseUtil;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * token认证失败（未认证时自定义响应）
 *
 * @author xiaokang
 * @since 2020-10-09
 */
@Component
public class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        return ResponseUtil.render(exchange, AuthErrorCode.TOKEN_INVALID);
    }

}
