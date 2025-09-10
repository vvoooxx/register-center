package com.example.registercenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器，用于返回Vue3控制台页面
 */
@Controller
public class IndexController {
    
    /**
     * 处理根路径请求，返回静态资源目录下的index.html
     * @return 控制台页面
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";  // 直接转发到静态资源目录下的index.html
    }
    
    /**
     * 处理旧index页面请求，重定向到新页面
     * @return 重定向到根路径
     */
    @GetMapping("/index")
    public String oldIndex() {
        return "redirect:/";  // 重定向到根路径
    }
}