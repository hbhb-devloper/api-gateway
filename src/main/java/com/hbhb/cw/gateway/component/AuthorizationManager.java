package com.hbhb.cw.gateway.component;

import com.hbhb.core.constants.AuthConstant;
import com.hbhb.redis.component.RedisHelper;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 鉴权管理器，用于判断是否有资源的访问权限
 *
 * @author xiaokang
 * @since 2020-10-07
 */
@Component
@Slf4j
@SuppressWarnings(value = {"unchecked"})
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Resource
    private RedisHelper redisHelper;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        ServerHttpRequest request = authorizationContext.getExchange().getRequest();
        PathMatcher pathMatcher = new AntPathMatcher();

        // 从缓存中获取资源权限列表
        Map<String, Object> resourceRolesMap = redisHelper.getMap(AuthConstant.RESOURCE_ROLES_KEY.value());
        Iterator<String> iterator = resourceRolesMap.keySet().iterator();

        // 请求路径匹配到的资源需要的角色权限集合authorities统计
        Set<String> authorities = new HashSet<>();
        while (iterator.hasNext()) {
            String pattern = iterator.next();
            if (pathMatcher.match(pattern, request.getURI().getPath())) {
                authorities.addAll((Collection<? extends String>) resourceRolesMap.get(pattern));
            }
        }

        return mono
                .filter(Authentication::isAuthenticated)
                .flatMapIterable(Authentication::getAuthorities)
                .map(GrantedAuthority::getAuthority)
                .any(roleId -> this.authorization(authorities, roleId))
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    /**
     * 鉴权方法
     *
     * @param authorities 请求该资源所需要角色的集合（从redis中获取）
     * @param roleId      请求用户的角色（格式：ROLE_{roleId}）
     * @return 是否有权限访问该资源
     */
    private boolean authorization(Set<String> authorities, String roleId) {
        // todo 暂时跳过鉴权，后续完善
        return true;
//        return authorities.contains(roleId);
    }

}
