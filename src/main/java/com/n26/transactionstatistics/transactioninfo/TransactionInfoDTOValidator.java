package com.n26.transactionstatistics.transactioninfo;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TransactionInfoDTOValidator implements Validator {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean supports(Class<?> clazz) {
		return TransactionInfoDTO.class.isAssignableFrom(clazz);

	}

	@Override
	public void validate(Object target, Errors errors) {
		TransactionInfoDTO transactionInfo = (TransactionInfoDTO) target;
		ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
		ZonedDateTime cutoffTime = now.minusSeconds(60);

		ZonedDateTime transactionDateTime = ZonedDateTime
				.ofInstant(Instant.ofEpochMilli(transactionInfo.getTimestamp()), ZoneId.of("UTC"));
		Duration duration = Duration.between(cutoffTime, transactionDateTime);
		if (duration.isNegative()) {
			logger.info("Duration is stale");
			errors.reject("204");

		}

	}

}
