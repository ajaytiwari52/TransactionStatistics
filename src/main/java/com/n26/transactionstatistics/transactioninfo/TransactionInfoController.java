package com.n26.transactionstatistics.transactioninfo;

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

	@RequestMapping(path = "/transactions", method = RequestMethod.POST)
	public DeferredResult<ResponseEntity<?>> postTransactions(@Valid @RequestBody TransactionInfoDTO transactionInfoDTO,
			BindingResult result) throws CutoffTimeExceededException {

		if (result.hasErrors()) {
			throw new CutoffTimeExceededException();
		}
		DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

		CompletableFuture.supplyAsync(() -> transactionInfoService.saveTransactionInfo(transactionInfoDTO), transactionPostExecutor)
				.whenComplete((p, throwable) ->
				{
					logger.info("Current Thread Name :{}", Thread.currentThread().getName());
					deferredResult.setResult(ResponseEntity.status(201).build());
				}

		
		);
		logger.info("Servlet thread released");
		logger.info("Servlet thread released :{}", Thread.activeCount());

		return deferredResult;

		// transactionInfoService.saveTransactionInfo(transactionInfoDTO);

	}

	@RequestMapping(path = "/statistics", method = RequestMethod.GET)
	public @ResponseBody DeferredResult<DoubleSummaryStatistics> getStatistics() {
		logger.info("Request received");

		DeferredResult<DoubleSummaryStatistics> deferredResult = new DeferredResult<>();

		CompletableFuture
				.supplyAsync(() -> transactionInfoService.getStatistics(ZonedDateTime.now(ZoneId.of("UTC"))), transactionStatisticsExecutor)
				.whenComplete((result, throwable) -> 
				{
					logger.info("Current Thread Name :{}", Thread.currentThread().getName());
					deferredResult.setResult(result);
				}
					
				
				);
		logger.info("Servlet thread released");
		logger.info("Servlet thread released :{}", Thread.activeCount());

		return deferredResult;

	}

}
