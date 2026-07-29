package com.wsc.security;

import com.wsc.rbac.WriteAuthorizationInterceptor;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.ServletContext;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 会话 Cookie（WSC_SESSION）、CORS（Vite credentials）、写权限拦截器。 优先 Java Config，避免改
 * application.yml。
 */
@Configuration
public class SecurityWebConfig implements WebMvcConfigurer {

  private final WriteAuthorizationInterceptor writeAuthorizationInterceptor;

  public SecurityWebConfig(WriteAuthorizationInterceptor writeAuthorizationInterceptor) {
    this.writeAuthorizationInterceptor = writeAuthorizationInterceptor;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
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
