package com.n26.transactionstatistics.transactioninfo;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kiaziz.transactionstatistics.transactioninfo.TransactionInfoDTO;

//@Profile("test")
@Component
public class TestHelper {
	
	@Autowired
	private ObjectMapper objectMapper;


	public long generateRandomTimeStamp(long startTimestamp, long endTimestamp) {
		Random random = new Random();
		long timestamp = startTimestamp + ((long) (random.nextDouble() * (endTimestamp - startTimestamp)));
		return timestamp;
	}

	public double generateRandomDouble(long minDouble, long maxDouble) {
		Random r = new Random();
		double randomDouble = minDouble + (maxDouble - minDouble) * r.nextDouble();
		return randomDouble;
	}
	
	public String getJSONRepresentation(TransactionInfoDTO transactionInfoDTO)
	{
		ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
		String json="";
		try {
			json= ow.writeValueAsString(transactionInfoDTO);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}
		return json;
		
	}

}
