package com.hbhb.cw.gateway.config;

import com.hbhb.core.constants.AuthConstant;
import com.hbhb.cw.gateway.component.AuthorizationManager;
import com.hbhb.cw.gateway.component.CustomServerAccessDeniedHandler;
import com.hbhb.cw.gateway.component.CustomServerAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.annotation.Resource;

import reactor.core.publisher.Mono;

/**
 * 资源服务器配置
 *
 * @author dxk
 */
@Configuration
@EnableWebFluxSecurity
public class ResourceServerConfig {

    @Resource
    private AuthorizationManager authorizationManager;
    @Resource
    private CustomServerAccessDeniedHandler customServerAccessDeniedHandler;
    @Resource
    private CustomServerAuthenticationEntryPoint customServerAuthenticationEntryPoint;
    @Resource
    private WhiteListConfig whiteListConfig;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.oauth2ResourceServer()
                .jwt().jwtAuthenticationConverter(jwtAuthenticationConverter());
        // 自定义处理jwt认证失败的结果
        http.oauth2ResourceServer().authenticationEntryPoint(customServerAuthenticationEntryPoint);
        http.authorizeExchange()
                // 免认证url资源（白名单）
                .pathMatchers(whiteListConfig.getUrls().toArray(new String[0])).permitAll()
                // option请求默认放行
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // 鉴权管理器配置
                .anyExchange().access(authorizationManager)
                .and().exceptionHandling()
                // 处理未授权
                .accessDeniedHandler(customServerAccessDeniedHandler)
                // 处理未认证
                .authenticationEntryPoint(customServerAuthenticationEntryPoint)
                .and()
                .csrf().disable();

        return http.build();
    }

    /**
     * ServerHttpSecurity没有将jwt中authorities的负载部分当做Authentication 需要把jwt的Claim中的authorities加入
     * 方案：重新定义ReactiveAuthenticationManager权限管理器，默认转换器JwtGrantedAuthoritiesConverter
     */
    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(AuthConstant.AUTHORITY_PREFIX.value());
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(AuthConstant.AUTHORITY_CLAIM_NAME.value());

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

}
