package com.shdata.datachain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Data Chain 统一启动入口（含接入端工作台业务）。 */
@EnableJpaAuditing
@SpringBootApplication
public class DataChainApplication {

  public static void main(String[] args) {
    SpringApplication.run(DataChainApplication.class, args);
  }
}
