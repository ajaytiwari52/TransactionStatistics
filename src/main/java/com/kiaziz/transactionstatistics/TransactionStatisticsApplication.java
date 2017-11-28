package com.kiaziz.transactionstatistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@EntityScan(
        basePackageClasses = {TransactionStatisticsApplication.class, Jsr310JpaConverters.class}
)
public class TransactionStatisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionStatisticsApplication.class, args);
	}
}
