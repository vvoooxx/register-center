package com.example.registercenter.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "service", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"serviceName", "serviceVersion", "ip", "port"}),
        @UniqueConstraint(columnNames = {"virtualDomain"})
})
public class RegisteredService {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String serviceName;
    
    @Column(nullable = false, length = 20)
    private String serviceVersion;
    
    @Column(nullable = false, length = 50)
    private String ip;
    
    @Column(nullable = false)
    private Integer port;
    
    @Column(nullable = false, length = 20)
    private String status = "UP";
    
    @Column(nullable = false)
    private LocalDateTime registerTime = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime lastHeartbeat = LocalDateTime.now();
    
    // 新增虚拟域名字段，用于通过虚拟域名访问服务
    @Column(nullable = true, length = 100)
    private String virtualDomain;
    
    // 限流相关字段
    @Column(nullable = false)
    private Integer maxRequestsPerSecond = 0; // 默认不限流
    
    @Column(nullable = false)
    private Boolean rateLimitEnabled = false; // 限流功能开关
    
    @Column(nullable = true)
    private String rateLimitErrorMessage = "服务暂时繁忙，请稍后再试"; // 限流提示消息
}