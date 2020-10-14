package com.hbhb.cw.gateway.util;

import com.hbhb.common.bean.ResponseVO;
import com.hbhb.cw.gateway.exception.enums.AuthErrorCode;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;

import reactor.core.publisher.Mono;

/**
 * @author xiaokang
 * @since 2020-06-20
 */
public class ResponseUtil {

    public static Mono<Void> render(ServerWebExchange exchange, AuthErrorCode errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setAccessControlAllowOrigin("*");
        response.getHeaders().setCacheControl(CacheControl.noCache());
        String body = JsonUtil.convert2Str(ResponseVO.error(errorCode.getCode(), errorCode.getMessage()));
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

}
