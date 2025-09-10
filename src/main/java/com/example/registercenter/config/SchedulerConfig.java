package com.example.registercenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类，用于启用Spring的定时任务功能
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 此类只需添加@EnableScheduling注解即可启用定时任务功能
}