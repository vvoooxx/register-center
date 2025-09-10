package com.example.registercenter.controller;

import com.example.registercenter.entity.RegisteredService;
import com.example.registercenter.service.ServiceRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceRegistryController {
    
    @Autowired
    private ServiceRegistryService serviceRegistryService;
    
    /**
     * 注册服务 - 前端使用，支持虚拟域名
     */
    @PostMapping
    public ResponseEntity<?> registerService(@RequestBody RegisteredService service) {

        try {
            RegisteredService registeredService = serviceRegistryService.registerService(
                    service.getServiceName(), 
                    service.getServiceVersion(), 
                    service.getIp(), 
                    service.getPort(),
                    service.getVirtualDomain());
            
            // 直接返回注册的服务对象，前端期望直接接收服务列表数据
            return ResponseEntity.ok(registeredService);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务注册失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 注销服务 - 前端使用
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deregisterService(@PathVariable Long id) {

        try {
            boolean success = serviceRegistryService.deregisterServiceById(id);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "服务注销成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务注销失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 发送心跳 - 前端使用
     */
    @PutMapping("/{id}/heartbeat")
    public ResponseEntity<?> sendHeartbeat(@PathVariable Long id) {

        try {
            boolean success = serviceRegistryService.sendHeartbeatById(id);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "心跳更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "心跳更新失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 获取所有服务列表 - 前端使用
     */
    @GetMapping
    public ResponseEntity<?> getAllServices() {

        try {
            List<RegisteredService> services = serviceRegistryService.listAllServices();
            
            // 直接返回服务列表，前端期望直接接收服务列表数据
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务列表查询失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 查找服务 - 保持原有功能，供其他客户端使用
     */
    @GetMapping("/find")
    public ResponseEntity<?> findServices(
            @RequestParam String serviceName,
            @RequestParam(required = false) String serviceVersion) {

        try {
            List<RegisteredService> services = serviceRegistryService.findServices(serviceName, serviceVersion);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "服务查询成功");
            response.put("data", services);
            response.put("total", services.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务查询失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 列出所有服务 - 保持原有功能，供其他客户端使用
     */
    @GetMapping("/list")
    public ResponseEntity<?> listAllServices() {

        try {
            List<RegisteredService> services = serviceRegistryService.listAllServices();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "服务列表查询成功");
            response.put("data", services);
            response.put("total", services.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务列表查询失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 注册服务 - 保持原有功能，供其他客户端使用
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerServiceOld(
            @RequestParam String serviceName,
            @RequestParam String serviceVersion,
            @RequestParam String ip,
            @RequestParam Integer port,
            @RequestParam(required = false) String virtualDomain) {

        try {
            RegisteredService registeredService = serviceRegistryService.registerService(
                    serviceName, serviceVersion, ip, port, virtualDomain);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "服务注册成功");
            response.put("data", registeredService);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务注册失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 注销服务 - 保持原有功能，供其他客户端使用
     */
    @DeleteMapping("/deregister")
    public ResponseEntity<?> deregisterServiceOld(
            @RequestParam String serviceName,
            @RequestParam String serviceVersion,
            @RequestParam String ip,
            @RequestParam Integer port) {

        try {
            boolean success = serviceRegistryService.deregisterService(
                    serviceName, serviceVersion, ip, port);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "服务注销成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务注销失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 根据虚拟域名查找服务
     */
    @GetMapping("/domain/{virtualDomain}")
    public ResponseEntity<?> findServiceByVirtualDomain(@PathVariable String virtualDomain) {
        try {
            RegisteredService service = serviceRegistryService.findServiceByVirtualDomain(virtualDomain);
            if (service != null && "UP".equals(service.getStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "服务查询成功");
                response.put("data", service);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "服务不存在或已离线");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "服务查询失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 发送心跳 - 保持原有功能，供其他客户端使用
     */
    @PutMapping("/heartbeat")
    public ResponseEntity<?> sendHeartbeatOld(
            @RequestParam String serviceName,
            @RequestParam String serviceVersion,
            @RequestParam String ip,
            @RequestParam Integer port) {

        try {
            boolean success = serviceRegistryService.sendHeartbeat(
                    serviceName, serviceVersion, ip, port);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "心跳更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "心跳更新失败: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /*
    // 通过ID发送心跳 - 前端使用
    @PutMapping("/{id}/heartbeat")
    public ResponseEntity<?> sendHeartbeatById(@PathVariable Long id) {
        
        try {
            boolean success = serviceRegistryService.sendHeartbeatById(id);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "心跳更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "服务不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "心跳更新失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    */
    
    /**
     * 更新服务虚拟域名
     */
    @PutMapping("/{id}/virtual-domain")
    public ResponseEntity<?> updateVirtualDomain(
            @PathVariable Long id,
            @RequestParam(required = false) String virtualDomain) {
        
        try {
            boolean success = serviceRegistryService.updateVirtualDomain(id, virtualDomain);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "虚拟域名更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "服务不存在或虚拟域名已被使用");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "虚拟域名更新失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}