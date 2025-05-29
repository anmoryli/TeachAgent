//package com.anmory.teachagent.config;
//
//import com.anmory.teachagent.interceptor.LoginInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * @author Anmory
// * @description 配置拦截器，拦截除登录接口外的所有请求
// * @date 2025-05-25 下午8:21
// */
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//    @Autowired
//    private LoginInterceptor loginInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(loginInterceptor)
//                .addPathPatterns("/**") // 拦截所有路径
//                .excludePathPatterns(
//                        "/admin/login", // 排除登录接口
//                        "/admin/login/**", // 排除登录相关子路径（如 /admin/login/verify）
//                        "/error", // 排除 Spring Boot 默认错误页面
//                        "/static/**", // 排除静态资源
//                        "/public/**", // 排除公共资源（如文档、图片）
//                        "/actuator/**" // 排除 Spring Boot Actuator 端点
//                );
//    }
//}