package com.official.lockr.global.config;

import com.official.lockr.global.http.HttpHeaderContextTaskDecorator;
import com.official.lockr.global.http.HttpHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private final HttpHeaders httpHeaders;

    public AsyncConfig(final HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @Override
    public Executor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("lockr-async-");
        executor.setTaskDecorator(new HttpHeaderContextTaskDecorator(httpHeaders));
        executor.initialize();
        return executor;
    }
}
