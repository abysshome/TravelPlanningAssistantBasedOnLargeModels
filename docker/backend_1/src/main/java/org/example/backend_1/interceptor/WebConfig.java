package org.example.backend_1.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//拦截器注册
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //自定义的拦截器对象
    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

    }
}