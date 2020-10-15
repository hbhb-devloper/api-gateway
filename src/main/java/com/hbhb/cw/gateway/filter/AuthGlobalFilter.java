package com.hbhb.cw.gateway.filter;

import com.hbhb.core.constants.AuthConstant;
import com.hbhb.core.utils.JsonUtil;
import com.hbhb.cw.gateway.enums.AuthErrorCode;
import com.hbhb.cw.gateway.util.ResponseUtil;
import com.hbhb.redis.component.RedisHelper;
import com.nimbusds.jose.JWSObject;

import org.apache.logging.log4j.util.Strings;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author xiaokang
 * @since 2020-10-09
 */
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private RedisHelper redisHelper;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(AuthConstant.JWT_TOKEN_HEADER.value());
        if (StringUtils.isEmpty(token)) {
            return chain.filter(exchange);
        }
        // 解析token
        token = token.replace(AuthConstant.JWT_TOKEN_PREFIX.value(), Strings.EMPTY);
        JWSObject jwsObject = JWSObject.parse(token);
        String payload = jwsObject.getPayload().toString();

        // 黑名单token校验（登出、修改密码时）
        // JWT唯一标识
        String jti = (String) JsonUtil.findByKey(payload, "jti");
        // 判断jti标识是否存在黑名单列表里
        Boolean isBlack = redisHelper.hasKey(AuthConstant.TOKEN_BLACKLIST_PREFIX.value() + jti);
        if (isBlack) {
            return ResponseUtil.render(exchange, AuthErrorCode.TOKEN_INVALID_OR_EXPIRED);
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(AuthConstant.JWT_PAYLOAD_KEY.value(), payload).build();
        exchange = exchange.mutate().request(request).build();
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
