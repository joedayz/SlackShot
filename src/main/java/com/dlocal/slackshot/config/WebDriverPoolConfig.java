package com.dlocal.slackshot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class WebDriverPoolConfig {

    @Value("${webdriver.pool.core-size:2}")
    private int corePoolSize;

    @Value("${webdriver.pool.max-size:5}")
    private int maxPoolSize;

    @Value("${webdriver.pool.queue-capacity:10}")
    private int queueCapacity;

    @Value("${webdriver.pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Bean("webDriverTaskExecutor")
    public Executor webDriverTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("WebDriver-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
} 