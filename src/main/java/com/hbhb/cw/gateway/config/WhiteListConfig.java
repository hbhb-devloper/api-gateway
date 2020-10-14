package com.hbhb.cw.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import lombok.Data;

/**
 * @author xiaokang
 * @since 2020-10-09
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "whitelist")
public class WhiteListConfig {
    private List<String> urls;
}
