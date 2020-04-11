package com.nowcoder.configuration;

import com.nowcoder.interceptor.LoginRequiredInterceptor;
import com.nowcoder.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Component
public class ToutiaoWebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    //先注册的拦截器优先处理
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有请求，检查是否有合法的ticket，如有，查出user信息放入HostHolder
        registry.addInterceptor(passportInterceptor);
        //拦截形如"/msg/*"路径的请求，消息中心的操作都需要登录，检查HostHolder是否有user信息
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/msg/*");
        super.addInterceptors(registry);
    }
}
