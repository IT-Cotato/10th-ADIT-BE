package com.adit.backend.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
	@Bean(name = "crawlingTaskExecutor")
	public ThreadPoolTaskExecutor crawlingTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);  // 기본 실행 대기 스레드 수
		executor.setMaxPoolSize(10);  // 동시 동작하는 최대 스레드 수
		executor.setQueueCapacity(500); // ThreadPool Queue 크기
		executor.setThreadNamePrefix("crawler-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 초과 요청에 대한 정책
		executor.setWaitForTasksToCompleteOnShutdown(true); // 시스템 종료 시 진행 중인 작업 완료 대기
		executor.setAwaitTerminationSeconds(60); // 최대 종료 대기 시간
		executor.initialize();
		return executor;
	}

	@Bean(name = "imageUploadExecutor")
	public Executor imageUploadExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10); // 기본적으로 실행할 스레드 수
		executor.setMaxPoolSize(20); // 최대 스레드 수
		executor.setQueueCapacity(50); // 대기 중인 작업 큐의 크기
		executor.setThreadNamePrefix("ImageUploadExecutor-"); // 스레드 이름 접두사
		executor.initialize();
		return executor;
	}
}
