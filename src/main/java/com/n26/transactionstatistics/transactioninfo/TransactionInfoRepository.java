package com.n26.transactionstatistics.transactioninfo;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionInfoRepository extends JpaRepository<TransactionInfo, Long> {

	//@Query("select c from TransactionInfo c where c.transactionTimeStamp > :startDate and c.transactionTimeStamp<:endDate")
	List<TransactionInfo>findByTransactionTimeStampBetween(ZonedDateTime startDate, ZonedDateTime endDate);
}
