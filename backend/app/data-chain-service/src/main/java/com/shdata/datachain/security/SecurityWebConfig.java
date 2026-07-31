package com.shdata.datachain.security;

import com.shdata.datachain.rbac.WriteAuthorizationInterceptor;
import java.util.Arrays;
import javax.servlet.SessionCookieConfig;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 会话 Cookie（WSC_SESSION）、CORS（前后端分仓部署）、写权限拦截器。
 * CORS 来源由 {@code wsc.cors.allowed-origins} / 环境变量 {@code WSC_CORS_ALLOWED_ORIGINS} 配置。
 */
@Configuration
public class SecurityWebConfig implements WebMvcConfigurer {

  private final WriteAuthorizationInterceptor writeAuthorizationInterceptor;

  @Value("${wsc.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
  private String allowedOrigins;

  public SecurityWebConfig(WriteAuthorizationInterceptor writeAuthorizationInterceptor) {
    this.writeAuthorizationInterceptor = writeAuthorizationInterceptor;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins =
        Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    registry
        .addMapping("/api/**")
        .allowedOrigins(origins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(writeAuthorizationInterceptor)
        .addPathPatterns("/api/v1/catalog/**");
  }

  @Bean
  public ServletContextInitializer wscSessionCookieInitializer() {
    return (ServletContext servletContext) -> {
      SessionCookieConfig cookie = servletContext.getSessionCookieConfig();
      cookie.setName("WSC_SESSION");
      cookie.setHttpOnly(true);
      cookie.setPath("/");
    };
  }
}
