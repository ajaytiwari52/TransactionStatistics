package com.n26.transactionstatistics.transactioninfo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.n26.transactionstatistics.transactioninfo.CutoffTimeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@ExceptionHandler(CutoffTimeExceededException.class)
	public void handleCutoffTimeExceededException() {
		logger.error("IOException handler executed");

	}

}
