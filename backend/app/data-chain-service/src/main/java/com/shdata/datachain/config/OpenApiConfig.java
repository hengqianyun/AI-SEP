package com.shdata.datachain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI dataChainOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Data Chain / WSC API")
                .description("Data Chain 后端 + 接入端工作台（WSC）接口文档")
                .version("2.0.0"));
  }
}
