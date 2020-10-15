package com.hbhb.cw.gateway.config;

import org.springdoc.core.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaokang
 * @since 2020-10-12
 */
@Configuration
@SuppressWarnings(value = {"all"})
@Slf4j
public class SwaggerConfig {

    @Value("${springdoc.title}")
    private String title;
    @Value("${springdoc.version}")
    private String version;
    @Value("${springdoc.description}")
    private String description;

    @Bean
    @Lazy(false)
    public List<GroupedOpenApi> apis(SwaggerUiConfigParameters swaggerUiConfigParameters, RouteDefinitionLocator locator) {
        List<GroupedOpenApi> groups = new ArrayList<>();
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls = new HashSet<>();

        definitions.stream()
                .filter(routeDefinition -> routeDefinition.getId().matches("^(?!ReactiveCompositeDiscoveryClient_).*$"))
                .forEach(routeDefinition -> swaggerUiConfigParameters.addGroup(routeDefinition.getId()));
        return groups;
    }

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description));
    }
}
