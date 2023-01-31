package me.bartosz1.monitoring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    @Value("${monitoring.production}")
    private boolean production;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggerInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/assets/**")
                .setCachePeriod(3600)
                .addResourceLocations("classpath:/static/assets/")
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!production)
            registry.addMapping("/**").allowedOrigins("http://localhost:5173").allowCredentials(true).allowedMethods("GET", "HEAD", "POST", "OPTIONS", "DELETE", "PATCH", "PUT");
    }
}
