package com.hbhb.cw.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbhb.core.bean.ApiResult;
import com.hbhb.core.enums.ResultCode;
import com.hbhb.core.utils.JsonUtil;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 全局响应体处理过滤器
 *
 * @author xiaokang
 * @since 2020-10-26
 */
@Component
@Slf4j
@SuppressWarnings(value = {"unchecked"})
public class ResponseGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 不需要做响应体封装的资源
     */
    private static final String[] EXCLUDE_PATH = {
            "/v3/api-docs"
    };

    private final ThreadLocal<ObjectMapper> mapperThreadLocal = ThreadLocal.withInitial(ObjectMapper::new);

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        ObjectMapper mapper = mapperThreadLocal.get();

        ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(@Nullable Publisher<? extends DataBuffer> body) {
                MediaType mediaType = getDelegate().getHeaders().getContentType();
                // 检查path白名单
                if (Arrays.stream(EXCLUDE_PATH).noneMatch(request.getURI().getPath()::contains)) {
                    if (body instanceof Flux) {
                        Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;
                        // 返回格式为application/json时，进行响应体封装
                        if (MediaType.APPLICATION_JSON_VALUE.equals(Objects.requireNonNull(mediaType).toString())){
                            return super.writeWith(fluxBody.buffer().map(dataBuffer -> {
                                // 如果响应体过大，会进行截断，出现乱码。join()方法可以合并所有的流，解决乱码问题
                                DataBuffer join = new DefaultDataBufferFactory().join(dataBuffer);
                                byte[] content = new byte[join.readableByteCount()];
                                join.read(content);
                                // 释放掉内存
                                DataBufferUtils.release(join);
                                // 重置返回参数
                                String result = response(mapper,  new String(content, StandardCharsets.UTF_8));
                                byte[] uppedContent = new String(
                                        result.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).getBytes();
                                // 修改后的返回参数必须重置长度，否则若修改后的参数长度超出原始参数长度时，会导致客户端接收到的参数丢失一部分
                                response.getHeaders().setContentLength(uppedContent.length);
                                return bufferFactory.wrap(uppedContent);
                            }));
                        }
                    }
                }
                return super.writeWith(Objects.requireNonNull(body));
            }
        };
        return chain.filter(exchange.mutate().response(decorator).build());
    }

    /**
     * 封装响应体
     * 微服务接口调用成功时，响应体没有做封装；异常时，响应体做了封装
     */
    private String response(ObjectMapper mapper, String result) {
        log.debug("\\n 响应值: {}", JsonUtil.prettyJson(result));
        try {
            // 如果是非json类型，则封装success返回
            if (!JsonUtil.isJson(result)) {
                return mapper.writeValueAsString(
                        ApiResult.success(result.replace("\"", "")));
            }
            // 接口调用成功，则封装success返回
            if (!result.contains(ResultCode.FAILED.msg())) {
                Object object = mapper.readValue(result, Object.class);
                return mapper.writeValueAsString(ApiResult.success(object));
            }
        } catch (Exception e1) {
            log.error("封装响应体失败", e1);
            try {
                return mapper.writeValueAsString(ApiResult.error(ResultCode.EXCEPTION.code(), result));
            } catch (Exception e2) {
                log.error("解析响应体异常信息失败", e2);
            }
        }
        return result;
    }
}
