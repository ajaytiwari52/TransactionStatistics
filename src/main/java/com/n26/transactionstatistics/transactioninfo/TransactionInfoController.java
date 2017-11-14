package com.n26.transactionstatistics.transactioninfo;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping()
public class TransactionInfoController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	@Qualifier("transactionStatisticsExecutorService")	
	private ExecutorService transactionStatisticsExecutor;

	@Autowired
	@Qualifier("transactionPostExecutorService")	
	private ExecutorService transactionPostExecutor;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new TransactionInfoDTOValidator());
	}

	@Autowired
	private TransactionInfoService transactionInfoService;
	
	/**
	 * 
	 * @param transactionInfoDTO
	 * @param result
	 * @return
	 * @throws CutoffTimeExceededException
	 * 
	 * Takes a TransactionInfo Object and saves it after DTO validation.
	 * The API ignores any transaction timestamp that are 60s older 
	 * than systemtimestamp(IN UTC)
	 * The API does not do anything if anytime stamp is in the future
	 * The API is non-blocking. The servlet thread is freed up immediately after the request 
	 * arrives. The processes of the request is delegated to internal thread mechanism
	 * using ExecutorService. This post API has its own dedicated threading mechanism, which 
	 * will be displayed in console as transactionPostExecutor. We are using binding validator to
	 * reject all transactions that are older than 60s, this way, the request doesn't even start the
	 * servlet thread
	 * 
	 */
	@RequestMapping(path = "/transactions", method = RequestMethod.POST)
	public DeferredResult<ResponseEntity<?>> postTransactions(@Valid @RequestBody TransactionInfoDTO transactionInfoDTO,
			BindingResult result) throws CutoffTimeExceededException {
		
		ZonedDateTime startTime = ZonedDateTime.now();

		logger.debug("Servlet Thread Started -{}",startTime);
		if (result.hasErrors()) {
			throw new CutoffTimeExceededException();
		}
		DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

		CompletableFuture.supplyAsync(() -> transactionInfoService.saveTransactionInfo(transactionInfoDTO), transactionPostExecutor)
				.whenComplete((p, throwable) ->
				{
					logger.debug("Current Thread Name :{}", Thread.currentThread().getName());
					deferredResult.setResult(ResponseEntity.status(201).build());
				}

		
		);
		ZonedDateTime endTime = ZonedDateTime.now();
		long durationInMillis = Duration.between(startTime, endTime).toMillis();
		logger.debug("Servlet thread released-{}",endTime);
		logger.info("Time taken for the POST Transaction IO to complete(in millis) - {}",durationInMillis);
		
	
		logger.debug("Number of Active Threads :{}", Thread.activeCount());
		if(durationInMillis>1000L)
		{
			logger.info("IO for POST was Blocked for thatn 1 Second-{} ",durationInMillis);
		}

		return deferredResult;

		

	}

	
	/**
	 * Returns the statistics of all the transactions that occured 
	 * in the last 60s.
	 * The API is non-blocking. The servlet thread is freed up immediately after the request 
	 * arrives. The processes of the request is delegated to internal thread mechanism
	 * using ExecutorService. This GET API has its own dedicated threading mechanism, which 
	 * will be displayed in console as transactionStatisticsExecutor
	 * 
	 * @return
	 */	
	
	@RequestMapping(path = "/statistics", method = RequestMethod.GET)
	public @ResponseBody DeferredResult<DoubleSummaryStatistics> getStatistics() {
		ZonedDateTime startTime = ZonedDateTime.now();
		

		logger.debug("Servlet Thread Started for statistics API Started -{}",startTime);


		DeferredResult<DoubleSummaryStatistics> deferredResult = new DeferredResult<>();

		CompletableFuture
				.supplyAsync(() -> transactionInfoService.getStatistics(ZonedDateTime.now(ZoneId.of("UTC"))), transactionStatisticsExecutor)
				.whenComplete((result, throwable) -> 
				{
					logger.info("Current Thread Name :{}", Thread.currentThread().getName());
					deferredResult.setResult(result);
				}
					
				
				);
		ZonedDateTime endTime = ZonedDateTime.now();
		long durationInMillis = Duration.between(startTime,endTime).toMillis();
		logger.debug("Servlet thread released -{}",endTime);
		logger.info("Time taken for the GET statistics IO to complete(in millis) - {}",durationInMillis);
		logger.info("Number of Active Threads :{}", Thread.activeCount());
		
		if(durationInMillis>1000L)
		{
			logger.info("IO for GET Blocked for thatn 1 Second-{} ",durationInMillis);
		}

		return deferredResult;

	}

}
