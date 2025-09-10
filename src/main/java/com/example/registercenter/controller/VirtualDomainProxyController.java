package com.example.registercenter.controller;

import com.example.registercenter.entity.RegisteredService;
import com.example.registercenter.service.ServiceRegistryService;
import com.example.registercenter.util.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * 虚拟域名代理控制器
 * 用于处理通过虚拟域名访问服务的请求转发
 */
@RestController
@RequestMapping("/proxy")
public class VirtualDomainProxyController {
    
    private static final Logger logger = LoggerFactory.getLogger(VirtualDomainProxyController.class);
    
    @Autowired
    private ServiceRegistryService serviceRegistryService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 通过虚拟域名或服务名代理请求到实际服务
     * 路径格式: /proxy/{virtualDomainOrServiceName}/**
     */
    @RequestMapping(value = "/{virtualDomainOrServiceName}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @PathVariable String virtualDomainOrServiceName,
            HttpServletRequest request,
            @RequestBody(required = false) byte[] requestBody) {
        
        try {
            // 查找对应的服务
            RegisteredService service = null;
            
            // 先尝试通过虚拟域名查找
            service = serviceRegistryService.findServiceByVirtualDomain(virtualDomainOrServiceName);
            
            // 如果虚拟域名未找到，尝试通过服务名查找
            if (service == null) {
                logger.info("未找到虚拟域名[{}]，尝试通过服务名查找", virtualDomainOrServiceName);
                List<RegisteredService> services = serviceRegistryService.findServices(virtualDomainOrServiceName, null);
                
                // 优先选择在线状态的服务
                for (RegisteredService s : services) {
                    if ("UP".equals(s.getStatus())) {
                        service = s;
                        break;
                    }
                }
                
                // 如果没有在线服务，选择第一个服务
                if (service == null && !services.isEmpty()) {
                    service = services.get(0);
                }
            }
            
            if (service == null) {
                logger.warn("未找到虚拟域名或服务名[{}]对应的服务", virtualDomainOrServiceName);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "未找到虚拟域名或服务名对应的服务");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            if (!"UP".equals(service.getStatus())) {
                logger.warn("服务[{}]已离线", service.getServiceName());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "服务已离线");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }
            
            // 检查限流
            if (service.getRateLimitEnabled() && service.getMaxRequestsPerSecond() > 0) {
                String serviceKey = service.getServiceName() + ":" + service.getId();
                boolean allowed = RateLimiter.isAllowed(serviceKey, service.getMaxRequestsPerSecond());
                
                if (!allowed) {
                    logger.warn("服务[{}]触发限流，当前请求数：{}/{}", 
                        service.getServiceName(), 
                        RateLimiter.getCurrentCount(serviceKey), 
                        service.getMaxRequestsPerSecond());
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", service.getRateLimitErrorMessage());
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
                }
            }
            
            // 构建目标URL
            String targetBaseUrl = "http://" + service.getIp() + ":" + service.getPort();
            
            // 获取请求路径中的剩余部分（除去/proxy/{virtualDomainOrServiceName}）
            String pathInfo = request.getRequestURI().replaceFirst(Pattern.quote("/proxy/" + virtualDomainOrServiceName), "");
            if (pathInfo.isEmpty()) {
                pathInfo = "/";
            }
            
            // 构建完整的目标URI
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(targetBaseUrl + pathInfo);
            
            // 复制原始请求的查询参数
            request.getParameterMap().forEach((key, values) -> {
                for (String value : values) {
                    uriBuilder.queryParam(key, value);
                }
            });
            
            URI targetUri = uriBuilder.build().toUri();
            logger.debug("转发请求到: {}，HTTP方法: {}", targetUri, request.getMethod());
            
            // 创建新的请求头
            HttpHeaders headers = new HttpHeaders();
            
            // 复制原始请求的头信息
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    String headerValue = headerValues.nextElement();
                    // 跳过Host头，由RestTemplate自动处理
                    if (!"Host".equalsIgnoreCase(headerName)) {
                        headers.add(headerName, headerValue);
                    }
                }
            }
            
            // 设置X-Forwarded-For头，表明请求经过转发
            String clientIp = request.getRemoteAddr();
            headers.add("X-Forwarded-For", clientIp);
            
            // 创建请求实体
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 根据原始请求方法转发请求
            ResponseEntity<byte[]> responseEntity;
            switch (request.getMethod()) {
                case "GET":
                    responseEntity = restTemplate.exchange(targetUri, HttpMethod.GET, requestEntity, byte[].class);
                    break;
                case "POST":
                    responseEntity = restTemplate.exchange(targetUri, HttpMethod.POST, requestEntity, byte[].class);
                    break;
                case "PUT":
                    responseEntity = restTemplate.exchange(targetUri, HttpMethod.PUT, requestEntity, byte[].class);
                    break;
                case "DELETE":
                    responseEntity = restTemplate.exchange(targetUri, HttpMethod.DELETE, requestEntity, byte[].class);
                    break;
                default:
                    logger.warn("不支持的HTTP方法: {}", request.getMethod());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "不支持的HTTP方法");
                    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
            }
            
            // 转发响应状态码、头信息和响应体
            return ResponseEntity
                    .status(responseEntity.getStatusCode())
                    .headers(responseEntity.getHeaders())
                    .body(responseEntity.getBody());
            
        } catch (Exception e) {
            logger.error("虚拟域名代理请求失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "代理请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}