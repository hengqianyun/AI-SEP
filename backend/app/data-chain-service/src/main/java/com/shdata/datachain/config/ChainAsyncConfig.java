package com.shdata.datachain.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** ChainMP 提交和轮询使用的独立线程资源，避免占用 Web 请求线程。 */
@Configuration
public class ChainAsyncConfig {

  /**
   * 创建上链 HTTP 调用线程池。
   *
   * @return 上链任务执行器
   */
  @Bean(name = "chainTaskExecutor")
  public Executor chainTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("chain-worker-");
    executor.initialize();
    return executor;
  }

  /**
   * 创建非阻塞重试调度器，重试间隔不通过 sleep 占用工作线程。
   *
   * @return 上链重试调度器
   */
  @Bean(name = "chainTaskScheduler")
  public ThreadPoolTaskScheduler chainTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(2);
    scheduler.setThreadNamePrefix("chain-retry-");
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(10);
    return scheduler;
  }
}
