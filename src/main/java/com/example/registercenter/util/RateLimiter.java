package com.example.registercenter.util;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流工具类，基于令牌桶算法实现
 */
public class RateLimiter {
    
    // 存储每个服务的请求计数
    private static final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    
    // 存储每个服务的上一次重置时间
    private static final ConcurrentHashMap<String, Long> lastResetTimes = new ConcurrentHashMap<>();
    
    /**
     * 检查是否允许请求通过
     * @param serviceKey 服务唯一标识
     * @param maxRequestsPerSecond 每秒最大请求数
     * @return 是否允许请求通过
     */
    public static boolean isAllowed(String serviceKey, int maxRequestsPerSecond) {
        if (maxRequestsPerSecond <= 0) {
            return true; // 不限流
        }
        
        long now = Instant.now().getEpochSecond();
        
        // 初始化计数器
        requestCounts.putIfAbsent(serviceKey, new AtomicInteger(0));
        lastResetTimes.putIfAbsent(serviceKey, now);
        
        long lastResetTime = lastResetTimes.get(serviceKey);
        
        // 如果已经过了一秒钟，重置计数器
        if (now > lastResetTime) {
            synchronized (RateLimiter.class) {
                // 双重检查锁定，防止并发重置
                if (now > lastResetTimes.get(serviceKey)) {
                    requestCounts.get(serviceKey).set(0);
                    lastResetTimes.put(serviceKey, now);
                }
            }
        }
        
        // 增加请求计数并检查是否超过限制
        int currentCount = requestCounts.get(serviceKey).incrementAndGet();
        return currentCount <= maxRequestsPerSecond;
    }
    
    /**
     * 重置指定服务的限流计数器
     * @param serviceKey 服务唯一标识
     */
    public static void reset(String serviceKey) {
        requestCounts.remove(serviceKey);
        lastResetTimes.remove(serviceKey);
    }
    
    /**
     * 重置所有服务的限流计数器
     */
    public static void resetAll() {
        requestCounts.clear();
        lastResetTimes.clear();
    }
    
    /**
     * 获取当前服务的请求计数
     * @param serviceKey 服务唯一标识
     * @return 请求计数
     */
    public static int getCurrentCount(String serviceKey) {
        return requestCounts.getOrDefault(serviceKey, new AtomicInteger(0)).get();
    }
}