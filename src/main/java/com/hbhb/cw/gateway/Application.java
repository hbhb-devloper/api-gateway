package com.hbhb.cw.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * @author dxk
 */
@OpenAPIDefinition(servers = {
        @Server(url = "https://gateway.yeexun.com.cn")
})
@RefreshScope
@EnableScheduling
@EnableFeignClients
@SpringCloudApplication
@ComponentScan("com.hbhb")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
