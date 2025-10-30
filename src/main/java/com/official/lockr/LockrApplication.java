package com.official.lockr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Lockr 애플리케이션 메인 클래스
 *
 * @EnableAsync: 비동기 처리 활성화 (ChatConsumer의 @Async 메서드)
 * @EnableRetry: 재시도 메커니즘 활성화 (ChatConsumer의 @Retryable 메서드)
 */
@EnableAsync
@EnableRetry
@SpringBootApplication
public class LockrApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockrApplication.class, args);
    }

}
