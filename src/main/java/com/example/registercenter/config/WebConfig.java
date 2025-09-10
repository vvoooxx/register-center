package com.example.registercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web配置类
 * 用于配置Web相关的Bean，如RestTemplate
 */
@Configuration
public class WebConfig {
    
    /**
     * 定义RestTemplate Bean，用于发送HTTP请求
     * 在VirtualDomainProxyController中用于转发请求到实际服务
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}