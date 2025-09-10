package com.example.registercenter.controller;

import com.example.registercenter.entity.RegisteredService;
import com.example.registercenter.service.ServiceRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 演示控制器，用于测试和调试功能
 */
@RestController
public class DemoController {

    @Autowired
    private ServiceRegistryService serviceRegistryService;

    /**
     * 手动注册8081端口服务的端点
     */
    @GetMapping("/demo/register-8081-service")
    public ResponseEntity<?> register8081Service() {
        try {
            // 注册一个运行在8081端口的服务
            RegisteredService service = serviceRegistryService.registerService(
                    "demo-service-8081",
                    "1.0.0",
                    "localhost",
                    8081
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "8081服务手动注册成功");
            response.put("service", service);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "8081服务手动注册失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 手动为8081端口服务发送心跳的端点
     */
    @GetMapping("/demo/send-heartbeat-to-8081")
    public ResponseEntity<?> sendHeartbeatTo8081() {
        try {
            // 向8081端口服务发送心跳
            boolean success = serviceRegistryService.sendHeartbeat(
                    "demo-service-8081",
                    "1.0.0",
                    "localhost",
                    8081
            );
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "向8081服务发送心跳成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "8081服务不存在或心跳发送失败");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "发送心跳失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}