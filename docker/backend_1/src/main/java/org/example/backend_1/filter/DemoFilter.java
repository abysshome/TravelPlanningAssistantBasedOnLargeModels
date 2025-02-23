package org.example.backend_1.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;


public class DemoFilter implements Filter {

    @Override//初始化方法
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        System.out.println("init执行");
    }

    @Override//拦截到请求之后，调用多次
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("拦截到请求");
        chain.doFilter(request,response);
    }

    @Override//销毁方法，只调用一次
    public void destroy() {
        Filter.super.destroy();
        System.out.println("执行销毁");
    }
}
