package com.n26.transactionstatistics.transactioninfo;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransactionInfoService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final static TransactionInfoDTOMapper mapper = new TransactionInfoDTOMapper();
	/*
	 * The amount of cutoff duration seconds. All transactions that occurred before
	 * this duration are not taking into consideration 
	 */
	@Value("${n26.transactionstatistics.cutoffduration:60}")
	private long cutoffDuration;

	@Autowired
	private TransactionInfoRepository repository;

	public TransactionInfo saveTransactionInfo(TransactionInfoDTO transactionInfoDTO) {

		TransactionInfo transactionInfo = mapper.apply(transactionInfoDTO);

		return repository.save(transactionInfo);

	}

	public DoubleSummaryStatistics getStatistics(ZonedDateTime now) {
		ZonedDateTime cutoffTime = now.minusSeconds(cutoffDuration);		
		List<TransactionInfo> transactionInfoList = getTransactionThatAreYoungerThanCutOffTime(repository.findAll(),now);
		DoubleSummaryStatistics transactionStatistics = transactionInfoList.stream()
				.collect(Collectors.summarizingDouble(TransactionInfo::getAmount));

		logger.info("transactionStatistics:{}", transactionInfoList);
		logger.info("now:{}", now);
		logger.info("cutoffTime:{}", cutoffTime);
		return transactionStatistics;

	}
	
	/**
	 * The API does allow future transactions to be registered. But, for statistical purposes, we are
	 * only taking the transactions that have occurred before the cutoff duration(in this case 60s).
	 * So, we have to first truncate the future transactions, and after that filter out 
	 * the transactions that are younger than the cutoff duration
	 * @param transactionsInfoList
	 * @param now
	 * @return
	 */

	private List<TransactionInfo> getTransactionThatAreYoungerThanCutOffTime(List<TransactionInfo> transactionsInfoList, ZonedDateTime now) {

		ZonedDateTime cutoffTime = now.minusSeconds(cutoffDuration);
		// This will contain will the transactions in the future
		List<TransactionInfo> futureTransactionsInfoList = new ArrayList<>();
		List<TransactionInfo> transactionsListYoungerThan60s = new ArrayList<>();

		transactionsInfoList.stream().forEach(p -> {
			Duration duration = Duration.between(now, p.getTransactionTimeStamp());
			if (!duration.isNegative()) {
				futureTransactionsInfoList.add(p);

			}

		});

		// Now we have only transactions that are in the past
		transactionsInfoList.removeAll(futureTransactionsInfoList);
		
		// We need only the transactions that are younger than cutoff duration
		transactionsInfoList.stream().forEach(p -> {
			Duration duration = Duration.between(cutoffTime, p.getTransactionTimeStamp());
			if (!duration.isNegative()) {
				transactionsListYoungerThan60s.add(p);

			}

		});

		return transactionsListYoungerThan60s;

	}

}
