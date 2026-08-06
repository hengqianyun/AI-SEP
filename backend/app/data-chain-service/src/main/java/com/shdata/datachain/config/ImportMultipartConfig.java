package com.shdata.datachain.config;

import javax.servlet.MultipartConfigElement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/**
 * 将 multipart 上限抬至略高于业务 10MB，以便业务层返回 {@code ERR_IMPORT_FILE_TOO_LARGE}，
 * 而非被容器默认 1MB 截断。
 */
@Configuration
public class ImportMultipartConfig {

  public static final long MAX_BYTES = 10L * 1024 * 1024;
  static final long CONTAINER_MAX_BYTES = 12L * 1024 * 1024;

  @Bean
  @ConditionalOnMissingBean(MultipartConfigElement.class)
  public MultipartConfigElement importMultipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize(DataSize.ofBytes(CONTAINER_MAX_BYTES));
    factory.setMaxRequestSize(DataSize.ofBytes(CONTAINER_MAX_BYTES));
    return factory.createMultipartConfig();
  }
}
