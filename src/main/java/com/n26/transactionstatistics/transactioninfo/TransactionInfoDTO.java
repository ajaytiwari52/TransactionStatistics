package com.n26.transactionstatistics.transactioninfo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class TransactionInfoDTO {

	private Double amount;

	private Long timestamp;

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "TransactionInfoDTO [amount=" + amount + ", timestamp=" + timestamp + "]";
	}

}
