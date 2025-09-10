package com.example.registercenter.service;

import com.example.registercenter.entity.RegisteredService;
import com.example.registercenter.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用启动初始化器，用于在应用启动时加载已保存的服务信息
 */
@Component
public class ServiceStartupInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(ServiceStartupInitializer.class);

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private HeartbeatMonitorService heartbeatMonitorService;

    /**
     * 在应用启动后执行的初始化逻辑
     */
    @Override
    public void run(ApplicationArguments args) {
        logger.info("应用启动初始化：开始加载已保存的服务信息");
        
        // 查询数据库中所有已保存的服务
        List<RegisteredService> savedServices = serviceRepository.findAll();
        
        if (savedServices.isEmpty()) {
            logger.info("应用启动初始化：当前没有已保存的服务");
            return;
        }
        
        logger.info("应用启动初始化：成功加载 {} 个已保存的服务", savedServices.size());
        
        // 记录加载的服务信息
        for (RegisteredService service : savedServices) {
            logger.debug("已加载服务: {} (v{}) - {}:{}", 
                    service.getServiceName(), 
                    service.getServiceVersion(), 
                    service.getIp(), 
                    service.getPort());
        }
        
        // 重置长时间未更新的服务状态，让心跳监测服务重新检测
        for (RegisteredService service : savedServices) {
            // 将超过1小时未更新心跳的服务标记为未知状态
            if (service.getLastHeartbeat().plusHours(1).isBefore(LocalDateTime.now())) {
                service.setStatus("UNKNOWN");
                serviceRepository.save(service);
                logger.debug("服务状态已重置为未知: {} (v{}) - {}:{}",
                        service.getServiceName(),
                        service.getServiceVersion(),
                        service.getIp(),
                        service.getPort());
            }
        }
        
        logger.info("应用启动初始化：服务加载完成，心跳监测服务将开始正常工作");
    }
}