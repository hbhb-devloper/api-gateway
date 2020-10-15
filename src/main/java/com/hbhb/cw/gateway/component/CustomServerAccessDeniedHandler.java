package com.hbhb.cw.gateway.component;

import com.hbhb.cw.gateway.enums.AuthErrorCode;
import com.hbhb.cw.gateway.util.ResponseUtil;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 无权访问资源（未授权时自定义响应）
 *
 * @author xiaokang
 * @since 2020-10-09
 */
@Component
public class CustomServerAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException e) {
        return ResponseUtil.render(exchange, AuthErrorCode.USER_ACCESS_UNAUTHORIZED);
    }
}
