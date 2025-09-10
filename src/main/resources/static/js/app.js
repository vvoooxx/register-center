// 创建Vue应用
const { createApp } = Vue;

// 创建主应用
const app = createApp({
    data() {
        return {
            // 滚动状态
            isScrolled: false,
            // 移动端菜单状态
            showMobileMenu: false,
            // 时间信息
            lastUpdateTime: new Date().toLocaleString('zh-CN'),
            // 统计数据
            totalServices: 2,
            onlineServices: 2,
            totalInstances: 2,
            avgResponseTime: 41,
            // 服务列表
            services: [
                {
                    id: 1,
                    serviceName: "demo-service-8081",
                    serviceVersion: "v1.0.0",
                    ip: "localhost",
                    port: 8081,
                    status: "UP",
                    registerTime: "2025-09-13T15:56:45",
                    lastHeartbeat: new Date().toISOString()
                },
                {
                    id: 2,
                    serviceName: "order-service",
                    serviceVersion: "vv001",
                    ip: "localhost",
                    port: 8081,
                    status: "UP",
                    registerTime: "2025-09-13T15:57:29",
                    lastHeartbeat: new Date().toISOString()
                }
            ],
            // 搜索和筛选
            searchQuery: '',
            statusFilter: 'all',
            // 注册服务模态框
            showRegisterModal: false,
            newService: {
                serviceName: '',
                serviceVersion: '',
                ip: '',
                port: ''
            },
            // 限流设置模态框
            showRateLimitModal: false,
            currentService: null,
            rateLimitConfig: {
                enabled: false,
                maxRequestsPerSecond: 0,
                errorMessage: '服务暂时繁忙，请稍后再试'
            },
            // 服务详情模态框
            showServiceDetailModal: false,
            // 虚拟域名设置模态框
            showVirtualDomainModal: false,
            virtualDomainConfig: {
                virtualDomain: ''
            },
            // 通知相关属性
            showNotification: false,
            notificationMessage: '',
            notificationType: 'info',
            notificationKey: 0,
            // 刷新相关属性
            isAutoRefreshEnabled: false,
            refreshInterval: 5000, // 默认5秒刷新一次
            refreshTimer: null
        }
    },
    
    computed: {
        // 过滤后的服务列表
        filteredServices() {
            return this.services.filter(service => {
                const matchesSearch = service.serviceName.toLowerCase().includes(this.searchQuery.toLowerCase())
                const matchesStatus = this.statusFilter === 'all' || service.status === this.statusFilter
                return matchesSearch && matchesStatus
            })
        },
        
        // 根据通知类型返回对应的样式类
        notificationTypeClasses() {
            switch (this.notificationType) {
                case 'success':
                    return 'bg-success text-white';
                case 'error':
                    return 'bg-danger text-white';
                case 'warning':
                    return 'bg-warning text-white';
                case 'info':
                default:
                    return 'bg-primary text-white';
            }
        },
        
        // 根据通知类型返回对应的图标
        notificationIcon() {
            switch (this.notificationType) {
                case 'success':
                    return 'fa-check-circle';
                case 'error':
                    return 'fa-times-circle';
                case 'warning':
                    return 'fa-exclamation-triangle';
                case 'info':
                default:
                    return 'fa-info-circle';
            }
        }
    },
    
    methods: {
        // 监听滚动事件
        handleScroll() {
            this.isScrolled = window.scrollY > 10;
        },
        
        // 切换移动端菜单
        toggleMobileMenu() {
            this.showMobileMenu = !this.showMobileMenu;
        },
        
        // 关闭移动端菜单
        closeMobileMenu() {
            this.showMobileMenu = false;
        },
        
        // 格式化日期时间
        formatDateTime(dateTime) {
            if (!dateTime) return '';
            const date = new Date(dateTime);
            return date.toLocaleString('zh-CN');
        },

        // 格式化心跳超时时间
        formatHeartbeatTimeout(lastHeartbeat) {
            if (!lastHeartbeat) return '';
            const now = new Date();
            const last = new Date(lastHeartbeat);
            const diffSeconds = Math.floor((now - last) / 1000);
            
            if (diffSeconds < 60) {
                return `${diffSeconds}秒前`;
            } else if (diffSeconds < 3600) {
                return `${Math.floor(diffSeconds / 60)}分钟前`;
            } else if (diffSeconds < 86400) {
                return `${Math.floor(diffSeconds / 3600)}小时前`;
            } else {
                return `${Math.floor(diffSeconds / 86400)}天前`;
            }
        },

        // 获取服务实例数
        getInstanceCount(serviceName) {
            return this.services.filter(s => s.serviceName === serviceName).length;
        },

        // 显示通知消息
        showNotificationMessage(message, type = 'info') {
            this.notificationMessage = message;
            this.notificationType = type;
            this.notificationKey += 1;
            this.showNotification = true;
            
            // 2秒后自动隐藏通知
            setTimeout(() => {
                this.showNotification = false;
            }, 2000);
        },

        // 注册服务
        async handleRegisterService(event) {
            // 阻止默认提交行为
            event.preventDefault();
            
            // 表单验证
            if (!this.newService.serviceName || !this.newService.serviceVersion || 
                !this.newService.ip || !this.newService.port) {
                this.showNotificationMessage('请填写完整的服务信息', 'warning');
                return;
            }

            // 端口验证
            const port = parseInt(this.newService.port);
            if (isNaN(port) || port < 1 || port > 65535) {
                this.showNotificationMessage('请输入有效的端口号（1-65535）', 'warning');
                return;
            }

            // IP格式验证
            const ipPattern = /^((25[0-5]|(2[0-4]|1\d|[1-9]|)\d)\.?\b){4}$/;
            if (!ipPattern.test(this.newService.ip)) {
                this.showNotificationMessage('请输入有效的IP地址格式', 'warning');
                return;
            }

            try {
                this.showNotificationMessage('正在注册服务...', 'info');
                
                const requestData = {
                    serviceName: this.newService.serviceName.trim(),
                    serviceVersion: this.newService.serviceVersion.trim(),
                    ip: this.newService.ip.trim(),
                    port: port
                };
                
                // 发送注册请求到后端API
                const response = await axios.post('/api/services', requestData);
                
                console.log('服务注册成功:', response.data);
                
                // 将新注册的服务添加到列表中
                if (response.data) {
                    this.services.push(response.data);
                    this.updateStats();
                }
                
                this.showNotificationMessage(`服务「${this.newService.serviceName}」注册成功`, 'success');
                
                // 重置表单并关闭模态框
                this.showRegisterModal = false;
                this.newService = { serviceName: '', serviceVersion: '', ip: '', port: '' };
                
            } catch (error) {
                console.error('注册服务失败:', error);
                
                let errorMessage = '注册服务失败，请重试';
                if (error.response) {
                    // 服务器返回错误
                    if (error.response.status === 409) {
                        errorMessage = '该服务已存在，请勿重复注册';
                    } else if (error.response.data && error.response.data.message) {
                        errorMessage = error.response.data.message;
                    }
                } else if (error.request) {
                    // 网络错误
                    errorMessage = '网络连接失败，请检查网络设置';
                }
                
                this.showNotificationMessage(errorMessage, 'error');
            }
        },

        // 更新统计数据
        updateStats() {
            // 获取去重后的服务名称列表
            const uniqueServices = [...new Set(this.services.map(s => s.serviceName))];
            this.totalServices = uniqueServices.length;
            
            // 计算在线服务数量
            const onlineServiceNames = [...new Set(this.services
                .filter(s => s.status === 'UP')
                .map(s => s.serviceName))];
            this.onlineServices = onlineServiceNames.length;
            
            // 计算实例总数
            this.totalInstances = this.services.length;
            
            // 这里简单设置平均响应时间，实际应该从服务数据中计算
            this.avgResponseTime = 41;
            
            // 更新最后更新时间
            this.lastUpdateTime = new Date().toLocaleString('zh-CN');
        },

        // 刷新服务列表
        async refreshServiceStatuses() {
            try {
                // 自动刷新时不显示通知消息
                if (!this.isAutoRefreshEnabled) {
                    this.showNotificationMessage('正在刷新服务状态...', 'info');
                }
                const response = await axios.get('/api/services');
                this.services = response.data || [];
                this.updateStats();
                if (!this.isAutoRefreshEnabled) {
                    this.showNotificationMessage('服务状态刷新成功', 'success');
                }
            } catch (error) {
                console.error('刷新服务状态失败:', error);
                if (!this.isAutoRefreshEnabled) {
                    this.showNotificationMessage('刷新服务状态失败，请重试', 'error');
                }
            }
        },
        
        // 切换自动刷新
        toggleAutoRefresh() {
            if (this.isAutoRefreshEnabled) {
                this.stopAutoRefresh();
                this.showNotificationMessage('自动刷新已关闭', 'info');
            } else {
                this.startAutoRefresh();
                this.showNotificationMessage(`已开启自动刷新，每${this.refreshInterval/1000}秒刷新一次`, 'success');
            }
        },
        
        // 开始自动刷新
        startAutoRefresh() {
            this.isAutoRefreshEnabled = true;
            // 立即刷新一次
            this.refreshServiceStatuses();
            // 设置定时器
            this.refreshTimer = setInterval(() => {
                this.refreshServiceStatuses();
            }, this.refreshInterval);
        },
        
        // 停止自动刷新
        stopAutoRefresh() {
            this.isAutoRefreshEnabled = false;
            if (this.refreshTimer) {
                clearInterval(this.refreshTimer);
                this.refreshTimer = null;
            }
        },
        
        // 打开限流设置模态框
        openRateLimitModal(service) {
            this.currentService = service;
            
            // 初始化限流配置
            this.rateLimitConfig = {
                enabled: service.rateLimitEnabled || false,
                maxRequestsPerSecond: service.maxRequestsPerSecond || 0,
                errorMessage: service.rateLimitErrorMessage || '服务暂时繁忙，请稍后再试'
            };
            
            this.showRateLimitModal = true;
        },
        
        // 关闭限流设置模态框
        closeRateLimitModal() {
            this.showRateLimitModal = false;
            this.currentService = null;
        },
        
        // 保存限流配置
        async saveRateLimitConfig() {
            if (!this.currentService) {
                return;
            }
            
            try {
                this.showNotificationMessage('正在保存限流配置...', 'info');
                
                const response = await axios.put(`/api/rate-limit/${this.currentService.id}`, null, {
                    params: {
                        enabled: this.rateLimitConfig.enabled,
                        maxRequestsPerSecond: this.rateLimitConfig.maxRequestsPerSecond,
                        errorMessage: this.rateLimitConfig.errorMessage
                    }
                });
                
                // 更新本地服务数据
                const serviceIndex = this.services.findIndex(s => s.id === this.currentService.id);
                if (serviceIndex !== -1) {
                    this.services[serviceIndex].rateLimitEnabled = this.rateLimitConfig.enabled;
                    this.services[serviceIndex].maxRequestsPerSecond = this.rateLimitConfig.maxRequestsPerSecond;
                    this.services[serviceIndex].rateLimitErrorMessage = this.rateLimitConfig.errorMessage;
                }
                
                this.showNotificationMessage('限流配置保存成功', 'success');
                this.closeRateLimitModal();
                
            } catch (error) {
                console.error('保存限流配置失败:', error);
                
                let errorMessage = '保存限流配置失败，请重试';
                if (error.response && error.response.data && error.response.data.message) {
                    errorMessage = error.response.data.message;
                }
                
                this.showNotificationMessage(errorMessage, 'error');
            }
        },
        
        // 查看服务详情
        viewServiceDetail(service) {
            this.currentService = service;
            this.showServiceDetailModal = true;
        },
        
        // 关闭服务详情模态框
        closeServiceDetailModal() {
            this.showServiceDetailModal = false;
            this.currentService = null;
        },
        
        // 发送心跳
        async sendHeartbeat(service) {
            try {
                this.showNotificationMessage(`正在向服务「${service.serviceName}」发送心跳...`, 'info');
                
                const response = await axios.put(`/api/services/${service.id}/heartbeat`);
                
                // 更新本地服务数据
                const serviceIndex = this.services.findIndex(s => s.id === service.id);
                if (serviceIndex !== -1) {
                    this.services[serviceIndex].lastHeartbeat = new Date().toISOString();
                    this.services[serviceIndex].status = 'UP';
                }
                
                this.showNotificationMessage(`服务「${service.serviceName}」心跳更新成功`, 'success');
                
            } catch (error) {
                console.error('发送心跳失败:', error);
                
                let errorMessage = '发送心跳失败，请重试';
                if (error.response && error.response.data && error.response.data.message) {
                    errorMessage = error.response.data.message;
                } else if (error.response && error.response.status === 404) {
                    errorMessage = '服务不存在';
                }
                
                this.showNotificationMessage(errorMessage, 'error');
            }
        },
        
        // 打开虚拟域名设置模态框
        openVirtualDomainModal(service) {
            this.currentService = service;
            this.virtualDomainConfig = {
                virtualDomain: service.virtualDomain || ''
            };
            this.showVirtualDomainModal = true;
        },
        
        // 关闭虚拟域名设置模态框
        closeVirtualDomainModal() {
            this.showVirtualDomainModal = false;
            this.currentService = null;
            this.virtualDomainConfig = {
                virtualDomain: ''
            };
        },
        
        // 保存虚拟域名配置
        saveVirtualDomainConfig() {
            if (!this.currentService || !this.currentService.id) {
                this.showNotificationMessage('服务信息无效', 'error');
                return;
            }
            
            const virtualDomain = this.virtualDomainConfig.virtualDomain || null;
            
            fetch(`/api/services/${this.currentService.id}/virtual-domain?virtualDomain=${encodeURIComponent(virtualDomain || '')}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // 更新本地服务列表中的虚拟域名
                    const serviceIndex = this.services.findIndex(s => s.id === this.currentService.id);
                    if (serviceIndex !== -1) {
                        this.services[serviceIndex].virtualDomain = virtualDomain;
                    }
                    this.showNotificationMessage('虚拟域名设置成功', 'success');
                } else {
                    this.showNotificationMessage(data.message || '虚拟域名设置失败', 'error');
                }
            })
            .catch(error => {
                console.error('设置虚拟域名失败:', error);
                this.showNotificationMessage('网络错误，设置虚拟域名失败', 'error');
            })
            .finally(() => {
                this.closeVirtualDomainModal();
            });
        },
        
        // 注销服务
        async deregisterService(service) {
            // 确认注销
            if (!confirm(`确定要注销服务「${service.serviceName}」吗？`)) {
                return;
            }
            
            try {
                this.showNotificationMessage(`正在注销服务「${service.serviceName}」...`, 'info');
                
                await axios.delete(`/api/services/${service.id}`);
                
                // 从本地服务列表中移除
                const serviceIndex = this.services.findIndex(s => s.id === service.id);
                if (serviceIndex !== -1) {
                    this.services.splice(serviceIndex, 1);
                    this.updateStats();
                }
                
                this.showNotificationMessage(`服务「${service.serviceName}」注销成功`, 'success');
                
            } catch (error) {
                console.error('注销服务失败:', error);
                
                let errorMessage = '注销服务失败，请重试';
                if (error.response && error.response.data && error.response.data.message) {
                    errorMessage = error.response.data.message;
                } else if (error.response && error.response.status === 404) {
                    errorMessage = '服务不存在';
                }
                
                this.showNotificationMessage(errorMessage, 'error');
            }
        }
    },
    
    // 组件挂载时执行
    mounted() {
        // 添加滚动事件监听器
        window.addEventListener('scroll', this.handleScroll);
        
        // 初始化统计数据
        this.updateStats();
        
        // 尝试从后端获取服务列表
        this.refreshServiceStatuses().catch(() => {
            console.log('使用本地模拟数据');
        });
    },
    
    // 组件卸载时执行
    beforeUnmount() {
        // 移除滚动事件监听器
        window.removeEventListener('scroll', this.handleScroll);
        // 停止自动刷新
        this.stopAutoRefresh();
    }
});

// 挂载应用
app.mount('#app');