package com.hbhb.cw.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hbhb.core.bean.ApiResult;
import com.hbhb.core.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xiaokang
 * @since 2020-10-26
 */
@Component
@Slf4j
@SuppressWarnings(value = {"unchecked"})
public class ResponseGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 将 List 数据以""分隔进行拼接
     */
    private static final Joiner JOINER = Joiner.on("");

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
                // swagger端点响应不需要封装
                if (!request.getURI().getPath().contains("/v3/api-docs")) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                            List<String> list = Lists.newArrayList();
                            // gateway 针对返回参数过长的情况下会分段返回，使用如下方式接受返回参数则可避免
                            dataBuffers.forEach(dataBuffer -> {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                // 释放掉内存
                                DataBufferUtils.release(dataBuffer);
                                list.add(new String(content, StandardCharsets.UTF_8));
                            });
                            // 将多次返回的参数拼接起来
                            String responseData = JOINER.join(list);
                            // 重置返回参数
                            String result = response(mapper, responseData);
                            byte[] uppedContent = new String(
                                    result.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).getBytes();
                            // 修改后的返回参数必须重置长度，否则若修改后的参数长度超出原始参数长度时，会导致客户端接收到的参数丢失一部分
                            response.getHeaders().setContentLength(uppedContent.length);
                            return bufferFactory.wrap(uppedContent);
                        }));
                    }
                }
                return super.writeWith(Objects.requireNonNull(body));
            }
        };
        return chain.filter(exchange.mutate().response(decorator).build());
    }

    /**
     * 封装响应体
     */
    private String response(ObjectMapper mapper, String result) {
        try {
            Map<String, Object> map = mapper.readValue(result, Map.class);
            return mapper.writeValueAsString(ApiResult.success(map));
        } catch (Exception e1) {
            try {
                return mapper.writeValueAsString(ApiResult.error(ResultCode.EXCEPTION.code(), result));
            } catch (Exception e2) {
                log.error("封装响应体失败：", e2);
            }
        }
        return result;
    }
}
