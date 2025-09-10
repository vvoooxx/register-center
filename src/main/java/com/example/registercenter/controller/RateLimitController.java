package com.example.registercenter.controller;

import com.example.registercenter.service.ServiceRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务限流管理控制器
 */
@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitController.class);
    
    @Autowired
    private ServiceRegistryService serviceRegistryService;
    
    /**
     * 获取服务的限流配置
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<?> getRateLimitConfig(@PathVariable Long serviceId) {
        try {
            Map<String, Object> rateLimitInfo = serviceRegistryService.getServiceRateLimit(serviceId);
            if (rateLimitInfo != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", rateLimitInfo);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("获取服务限流配置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取服务限流配置失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 设置服务的限流配置
     */
    @PutMapping("/{serviceId}")
    public ResponseEntity<?> setRateLimitConfig(
            @PathVariable Long serviceId,
            @RequestParam boolean enabled,
            @RequestParam int maxRequestsPerSecond,
            @RequestParam(required = false) String errorMessage) {
        try {
            boolean result = serviceRegistryService.setServiceRateLimit(serviceId, enabled, maxRequestsPerSecond, errorMessage);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", enabled ? "服务限流配置已启用" : "服务限流配置已更新");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("设置服务限流配置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置服务限流配置失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 启用服务限流
     */
    @PostMapping("/{serviceId}/enable")
    public ResponseEntity<?> enableRateLimit(
            @PathVariable Long serviceId,
            @RequestParam int maxRequestsPerSecond) {
        try {
            boolean result = serviceRegistryService.enableRateLimit(serviceId, maxRequestsPerSecond);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "服务限流已启用");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("启用服务限流失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "启用服务限流失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 禁用服务限流
     */
    @PostMapping("/{serviceId}/disable")
    public ResponseEntity<?> disableRateLimit(@PathVariable Long serviceId) {
        try {
            boolean result = serviceRegistryService.disableRateLimit(serviceId);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "服务限流已禁用");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("禁用服务限流失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "禁用服务限流失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}