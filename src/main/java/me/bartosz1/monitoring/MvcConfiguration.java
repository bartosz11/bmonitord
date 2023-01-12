package me.bartosz1.monitoring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
//this exists just so we can use our fancy request logging interceptor which prevents a lot of boilerplate :)
public class MvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggerInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").setCachePeriod(3600).addResourceLocations("classpath:/static/").resourceChain(true).addResolver(new PathResourceResolver());
    }
}
