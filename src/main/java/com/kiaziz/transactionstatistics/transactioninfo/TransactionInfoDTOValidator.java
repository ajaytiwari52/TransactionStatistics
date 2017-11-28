package com.kiaziz.transactionstatistics.transactioninfo;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TransactionInfoDTOValidator implements Validator {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/*
	 * The amount of cutoff duration seconds. All transactions that occurred before
	 * this duration are not taking into consideration 
	 */
	@Value("${n26.transactionstatistics.cutoffduration:60}")
	private long cutoffDuration;

	@Override
	public boolean supports(Class<?> clazz) {
		return TransactionInfoDTO.class.isAssignableFrom(clazz);

	}
	/**
	 * The validator is rejecting all timestamps that are older than
	 * the cutoff timestamp in this case 60s
	 */
	@Override
	public void validate(Object target, Errors errors) {
		TransactionInfoDTO transactionInfo = (TransactionInfoDTO) target;
		ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
		ZonedDateTime cutoffTime = now.minusSeconds(60);

		ZonedDateTime transactionDateTime = ZonedDateTime
				.ofInstant(Instant.ofEpochMilli(transactionInfo.getTimestamp()), ZoneId.of("UTC"));
		Duration duration = Duration.between(cutoffTime, transactionDateTime);
		if (duration.isNegative()) {			
			errors.reject("204");

		}

	}

}
