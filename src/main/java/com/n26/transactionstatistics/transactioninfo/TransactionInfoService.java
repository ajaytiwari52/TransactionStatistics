package com.n26.transactionstatistics.transactioninfo;

import java.time.ZonedDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionInfoService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	TransactionInfoDTOMapper mapper = new TransactionInfoDTOMapper();

	@Autowired
	private TransactionInfoRepository repository;

	public TransactionInfo saveTransactionInfo(TransactionInfoDTO transactionInfoDTO) {

		TransactionInfo transactionInfo = mapper.apply(transactionInfoDTO);

		return repository.save(transactionInfo);

	}

	public DoubleSummaryStatistics getStatistics(ZonedDateTime now) {
		ZonedDateTime cutoffTime = now.minusMinutes(1);
		List<TransactionInfo> transactionInfoList = repository.findByTransactionTimeStampBetween(now, cutoffTime);
		List<TransactionInfo> transactionInfoList1 = repository.findAll();
		DoubleSummaryStatistics transactionStatistics = transactionInfoList1.stream()
				.collect(Collectors.summarizingDouble(TransactionInfo::getAmount));

		logger.info("transactionStatistics:{}", transactionStatistics);
		return transactionStatistics;

	}

}
