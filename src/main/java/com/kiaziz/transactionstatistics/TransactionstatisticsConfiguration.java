package com.kiaziz.transactionstatistics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Configuration	
public class TransactionstatisticsConfiguration {

	/**
	 * Dedicated Thread Modeling to handle POST Requests for incoming
	 * Transactions	 
	 */
	@Bean
	public ExecutorService transactionPostExecutorService() {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
		        .setNameFormat("transactionPostExecutor-%d")
		        .setDaemon(true)
		        .build();
		ExecutorService es = Executors.newFixedThreadPool(2,threadFactory);
		return es;
	}
	
	/**
	 * Dedicated Thread Modeling to handle GET Statistics
	 */
	@Bean()
	public ExecutorService transactionStatisticsExecutorService() {
		final ThreadFactory threadFactory = new ThreadFactoryBuilder()
		        .setNameFormat("transactionStatisticsExecutor-%d")
		        .setDaemon(true)
		        .build();
		ExecutorService es = Executors.newFixedThreadPool(2,threadFactory);
		return es;
	}

}
