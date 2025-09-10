package com.example.registercenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 虚拟域名测试控制器
 * 提供简单的API端点用于测试虚拟域名功能
 */
@RestController
public class VirtualDomainTestController {
    
    /**
     * 测试端点 - 返回基本信息
     */
    @GetMapping("/virtual-domain-test/info")
    public ResponseEntity<?> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "虚拟域名测试服务正常运行");
        response.put("serviceType", "register-center");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 测试端点 - 返回当前时间
     */
    @GetMapping("/virtual-domain-test/time")
    public ResponseEntity<?> getCurrentTime() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("currentTime", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 代理测试端点 - 可以通过虚拟域名访问
     */
    @GetMapping("/proxy-test/echo")
    public ResponseEntity<?> echo() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Hello from proxy test endpoint");
        response.put("description", "This endpoint can be accessed through virtual domain");
        return ResponseEntity.ok(response);
    }
}