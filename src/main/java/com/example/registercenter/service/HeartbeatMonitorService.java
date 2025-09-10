package com.example.registercenter.service;

import com.example.registercenter.entity.RegisteredService;
import com.example.registercenter.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 心跳监测服务，用于自动检测服务是否在线
 */
@Service
public class HeartbeatMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatMonitorService.class);

    @Autowired
    private ServiceRepository serviceRepository;

    // 心跳超时阈值（秒），设置为90秒避免网络波动导致的误判
    private static final long HEARTBEAT_TIMEOUT_SECONDS = 90;
    
    // 服务心跳失败计数器，用于记录连续心跳失败次数
    private final Map<String, Integer> heartbeatFailureCount = new ConcurrentHashMap<>();
    
    // 允许的最大心跳失败次数，超过此值将进行额外处理
    private static final int MAX_HEARTBEAT_FAILURES = 3;

    /**
     * 定时检查所有服务的心跳状态
     * 使用@Scheduled注解配置定时任务，每5秒执行一次
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkHeartbeats() {
        // 获取所有注册的服务
        List<RegisteredService> allServices = serviceRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        logger.debug("开始心跳检查，当前共有 {} 个注册服务", allServices.size());
        
        for (RegisteredService service : allServices) {
            // 服务唯一标识
            String serviceKey = service.getServiceName() + ":" + service.getServiceVersion() + ":" + service.getIp() + ":" + service.getPort();
            
            // 计算最后心跳时间与当前时间的差值
            Duration duration = Duration.between(service.getLastHeartbeat(), now);
            long secondsSinceLastHeartbeat = duration.getSeconds();
            
            // 检查服务是否已经标记为离线
            if ("DOWN".equals(service.getStatus())) {
                logger.debug("跳过离线服务: {}", serviceKey);
                continue;
            }
            
            // 如果超过心跳超时阈值，则更新服务状态为离线
            if (secondsSinceLastHeartbeat > HEARTBEAT_TIMEOUT_SECONDS) {
                // 增加心跳失败计数
                int failureCount = heartbeatFailureCount.getOrDefault(serviceKey, 0) + 1;
                heartbeatFailureCount.put(serviceKey, failureCount);
                
                // 首次检测到心跳超时，将服务标记为离线
                if (failureCount == 1) {
                    service.setStatus("DOWN");
                    serviceRepository.save(service);
                    logger.info("服务离线: {} (v{}) - {}:{}", 
                            service.getServiceName(), 
                            service.getServiceVersion(), 
                            service.getIp(), 
                            service.getPort());
                } else if (failureCount >= MAX_HEARTBEAT_FAILURES) {
                    // 超过最大失败次数，记录警告日志
                    logger.warn("服务持续离线，可能需要人工干预: {} (v{}) - {}:{}, 已连续失败{}次", 
                            service.getServiceName(), 
                            service.getServiceVersion(), 
                            service.getIp(), 
                            service.getPort(), 
                            failureCount);
                }
            } else {
                // 心跳正常，重置失败计数
                heartbeatFailureCount.remove(serviceKey);
                
                // 如果服务状态是UNKNOWN，更新为UP
                if ("UNKNOWN".equals(service.getStatus())) {
                    service.setStatus("UP");
                    serviceRepository.save(service);
                    logger.info("服务已恢复在线: {} (v{}) - {}:{}", 
                            service.getServiceName(), 
                            service.getServiceVersion(), 
                            service.getIp(), 
                            service.getPort());
                }
            }
        }
    }
}