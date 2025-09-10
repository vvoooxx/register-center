package com.example.registercenter.service;

import com.example.registercenter.entity.RegisteredService;
import com.example.registercenter.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ServiceRegistryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryService.class);
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    /**
     * 注册服务 - 修改为当IP+端口一致时当做修改，支持虚拟域名
     */
    @Transactional
    public RegisteredService registerService(String serviceName, String serviceVersion, String ip, Integer port) {
        // 调用带虚拟域名参数的重载方法，默认传入null
        return registerService(serviceName, serviceVersion, ip, port, null);
    }
    
    /**
     * 注册服务 - 支持虚拟域名的重载方法
     * 优化：支持查找并重新激活相同IP+端口+服务名的离线服务
     */
    @Transactional
    public RegisteredService registerService(String serviceName, String serviceVersion, String ip, Integer port, String virtualDomain) {
        // 优先查找完全匹配的服务（相同IP+端口+服务名+版本）
        Optional<RegisteredService> exactMatch = serviceRepository.findByServiceNameAndServiceVersionAndIpAndPort(
                serviceName, serviceVersion, ip, port);
        
        if (exactMatch.isPresent()) {
            // 存在完全匹配的服务，重新激活它
            RegisteredService service = exactMatch.get();
            service.setStatus("UP");
            service.setLastHeartbeat(LocalDateTime.now());
            // 更新虚拟域名
            service.setVirtualDomain(virtualDomain);
            logger.info("重新激活已注销的服务: {} (v{}) - {}:{}", 
                    service.getServiceName(), service.getServiceVersion(), service.getIp(), service.getPort());
            return serviceRepository.save(service);
        }
        
        // 检查是否存在相同IP+端口的服务
        List<RegisteredService> allServices = serviceRepository.findAll();
        RegisteredService existingServiceWithSameIpPort = null;
        List<RegisteredService> otherServicesWithSameIpPort = new ArrayList<>();
        
        for (RegisteredService service : allServices) {
            if (service.getIp().equals(ip) && service.getPort().equals(port)) {
                if (existingServiceWithSameIpPort == null) {
                    existingServiceWithSameIpPort = service;
                } else {
                    otherServicesWithSameIpPort.add(service);
                }
            }
            // 检查是否存在相同虚拟域名的服务
            if (virtualDomain != null && virtualDomain.equals(service.getVirtualDomain())) {
                // 如果有相同虚拟域名的服务，也添加到待删除列表
                otherServicesWithSameIpPort.add(service);
            }
        }
        
        if (existingServiceWithSameIpPort != null) {
            // 存在相同IP+端口的服务，更新第一个找到的服务的属性
            existingServiceWithSameIpPort.setServiceName(serviceName);
            existingServiceWithSameIpPort.setServiceVersion(serviceVersion);
            existingServiceWithSameIpPort.setStatus("UP");
            existingServiceWithSameIpPort.setLastHeartbeat(LocalDateTime.now());
            // 更新虚拟域名
            existingServiceWithSameIpPort.setVirtualDomain(virtualDomain);
            
            // 删除其他相同IP+端口的服务实例，确保唯一性
            if (!otherServicesWithSameIpPort.isEmpty()) {
                for (RegisteredService service : otherServicesWithSameIpPort) {
                    serviceRepository.deleteById(service.getId());
                }
            }
            
            return serviceRepository.save(existingServiceWithSameIpPort);
        } else {
            // 创建新服务
            RegisteredService service = new RegisteredService();
            service.setServiceName(serviceName);
            service.setServiceVersion(serviceVersion);
            service.setIp(ip);
            service.setPort(port);
            service.setStatus("UP");
            service.setRegisterTime(LocalDateTime.now());
            service.setLastHeartbeat(LocalDateTime.now());
            // 设置虚拟域名
            service.setVirtualDomain(virtualDomain);
            return serviceRepository.save(service);
        }
    }
    
    /**
     * 注销服务 - 修改为只更新状态为DOWN而不删除
     */
    @Transactional
    public boolean deregisterService(String serviceName, String serviceVersion, String ip, Integer port) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findByServiceNameAndServiceVersionAndIpAndPort(
                serviceName, serviceVersion, ip, port);
        
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            service.setStatus("DOWN");
            serviceRepository.save(service);
            logger.info("服务已手动设置为离线: {} (v{}) - {}:{}", 
                    service.getServiceName(), service.getServiceVersion(), service.getIp(), service.getPort());
            return true;
        }
        return false;
    }
    
    /**
     * 根据虚拟域名查找服务
     */
    public RegisteredService findServiceByVirtualDomain(String virtualDomain) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findByVirtualDomain(virtualDomain);
        return serviceOpt.orElse(null);
    }
    
    /**
     * 发送心跳
     */
    @Transactional
    public boolean sendHeartbeat(String serviceName, String serviceVersion, String ip, Integer port) {
        int updatedCount = serviceRepository.updateHeartbeat(
                serviceName, serviceVersion, ip, port, LocalDateTime.now());
        return updatedCount > 0;
    }
    
    /**
     * 查找服务
     */
    public List<RegisteredService> findServices(String serviceName, String serviceVersion) {
        if (serviceVersion != null && !serviceVersion.isEmpty()) {
            List<RegisteredService> services = serviceRepository.findByServiceNameAndServiceVersion(serviceName, serviceVersion);
            // 确保返回的服务对象包含完整的限流信息
            return services;
        } else {
            List<RegisteredService> services = serviceRepository.findByServiceName(serviceName);
            // 确保返回的服务对象包含完整的限流信息
            return services;
        }
    }
    
    /**
     * 列出所有服务
     */
    public List<RegisteredService> listAllServices() {
        List<RegisteredService> services = serviceRepository.findAll();
        // 确保返回的服务对象包含完整的限流信息
        return services;
    }
    
    /**
     * 更新服务状态
     */
    @Transactional
    public boolean updateServiceStatus(String serviceName, String serviceVersion, String ip, Integer port, String status) {
        int updatedCount = serviceRepository.updateStatus(serviceName, serviceVersion, ip, port, status);
        return updatedCount > 0;
    }
    
    /**
     * 检查服务是否存在
     */
    public boolean serviceExists(String serviceName, String serviceVersion, String ip, Integer port) {
        return serviceRepository.findByServiceNameAndServiceVersionAndIpAndPort(
                serviceName, serviceVersion, ip, port).isPresent();
    }
    
    /**
     * 通过ID注销服务 - 修改为只更新状态为DOWN而不删除
     */
    @Transactional
    public boolean deregisterServiceById(Long id) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findById(id);
        
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            service.setStatus("DOWN");
            serviceRepository.save(service);
            logger.info("服务已通过ID手动设置为离线: {} (v{}) - {}:{}", 
                    service.getServiceName(), service.getServiceVersion(), service.getIp(), service.getPort());
            return true;
        }
        return false;
    }
    
    /**
     * 通过ID发送心跳
     */
    @Transactional
    public boolean sendHeartbeatById(Long id) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            service.setLastHeartbeat(LocalDateTime.now());
            // 发送心跳时自动将服务状态设置为在线
            service.setStatus("UP");
            serviceRepository.save(service);
            return true;
        }
        return false;
    }

    /**
     * 检查服务是否在线
     */
    public boolean isServiceOnline(Long id) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            return "UP".equals(service.getStatus());
        }
        return false;
    }

    /**
     * 将服务设置为在线状态
     */
    @Transactional
    public boolean setServiceOnline(Long id) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            service.setStatus("UP");
            service.setLastHeartbeat(LocalDateTime.now());
            serviceRepository.save(service);
            return true;
        }
        return false;
    }
    
    /**
     * 设置服务限流配置
     */
    @Transactional
    public boolean setServiceRateLimit(Long id, boolean enabled, int maxRequestsPerSecond, String errorMessage) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            service.setRateLimitEnabled(enabled);
            service.setMaxRequestsPerSecond(maxRequestsPerSecond);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                service.setRateLimitErrorMessage(errorMessage);
            }
            serviceRepository.save(service);
            return true;
        }
        return false;
    }
    
    /**
     * 获取服务限流配置
     */
    public Map<String, Object> getServiceRateLimit(Long id) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            Map<String, Object> rateLimitInfo = new HashMap<>();
            rateLimitInfo.put("enabled", service.getRateLimitEnabled());
            rateLimitInfo.put("maxRequestsPerSecond", service.getMaxRequestsPerSecond());
            rateLimitInfo.put("errorMessage", service.getRateLimitErrorMessage());
            return rateLimitInfo;
        }
        return null;
    }
    
    /**
     * 启用服务限流
     */
    @Transactional
    public boolean enableRateLimit(Long id, int maxRequestsPerSecond) {
        return setServiceRateLimit(id, true, maxRequestsPerSecond, null);
    }
    
    /**
     * 禁用服务限流
     */
    @Transactional
    public boolean disableRateLimit(Long id) {
        return setServiceRateLimit(id, false, 0, null);
    }
    
    /**
     * 更新服务虚拟域名
     */
    @Transactional
    public boolean updateVirtualDomain(Long id, String virtualDomain) {
        Optional<RegisteredService> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isPresent()) {
            RegisteredService service = serviceOpt.get();
            
            // 检查虚拟域名是否已被其他服务使用
            if (virtualDomain != null && !virtualDomain.isEmpty()) {
                Optional<RegisteredService> existingServiceOpt = serviceRepository.findByVirtualDomain(virtualDomain);
                if (existingServiceOpt.isPresent() && !existingServiceOpt.get().getId().equals(id)) {
                    // 虚拟域名已被其他服务使用
                    return false;
                }
            }
            
            // 更新虚拟域名
            service.setVirtualDomain(virtualDomain);
            serviceRepository.save(service);
            return true;
        }
        return false;
    }
}