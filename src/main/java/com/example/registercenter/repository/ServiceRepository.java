package com.example.registercenter.repository;

import com.example.registercenter.entity.RegisteredService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<RegisteredService, Long> {
    
    // 根据服务名称和版本查找服务
    List<RegisteredService> findByServiceNameAndServiceVersion(String serviceName, String serviceVersion);
    
    // 根据服务名称查找所有版本的服务
    List<RegisteredService> findByServiceName(String serviceName);
    
    // 查找特定的服务实例
    Optional<RegisteredService> findByServiceNameAndServiceVersionAndIpAndPort(
            String serviceName, String serviceVersion, String ip, Integer port);
    
    // 根据IP和端口查找服务
    List<RegisteredService> findByIpAndPort(String ip, Integer port);
    
    // 根据虚拟域名查找服务
    Optional<RegisteredService> findByVirtualDomain(String virtualDomain);
    
    // 更新心跳时间
    @Modifying
    @Query("UPDATE RegisteredService s SET s.lastHeartbeat = :time WHERE s.serviceName = :serviceName AND s.serviceVersion = :serviceVersion AND s.ip = :ip AND s.port = :port")
    int updateHeartbeat(@Param("serviceName") String serviceName,
                        @Param("serviceVersion") String serviceVersion,
                        @Param("ip") String ip,
                        @Param("port") Integer port,
                        @Param("time") LocalDateTime time);
    
    // 更新服务状态
    @Modifying
    @Query("UPDATE RegisteredService s SET s.status = :status WHERE s.serviceName = :serviceName AND s.serviceVersion = :serviceVersion AND s.ip = :ip AND s.port = :port")
    int updateStatus(@Param("serviceName") String serviceName,
                    @Param("serviceVersion") String serviceVersion,
                    @Param("ip") String ip,
                    @Param("port") Integer port,
                    @Param("status") String status);
    
    // 删除服务实例
    int deleteByServiceNameAndServiceVersionAndIpAndPort(
            String serviceName, String serviceVersion, String ip, Integer port);
}