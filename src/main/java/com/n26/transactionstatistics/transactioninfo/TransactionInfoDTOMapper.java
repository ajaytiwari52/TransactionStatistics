package com.n26.transactionstatistics.transactioninfo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class TransactionInfoDTOMapper implements Function<TransactionInfoDTO,TransactionInfo>{

	@Override
	public TransactionInfo apply(TransactionInfoDTO transactionInfoDTO) {
		
		Instant i = Instant.ofEpochMilli(transactionInfoDTO.getTimestamp());
		ZonedDateTime transactionTimeStamp = ZonedDateTime.ofInstant(i, ZoneId.of("UTC"));		
		TransactionInfo transactionInfo= new TransactionInfo(transactionInfoDTO.getAmount(),transactionTimeStamp);
		return transactionInfo;
	}

}
