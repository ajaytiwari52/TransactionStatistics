package com.n26.transactionstatistics.transactioninfo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.kiaziz.transactionstatistics.transactioninfo.TransactionInfo;
import com.kiaziz.transactionstatistics.transactioninfo.TransactionInfoRepository;
import com.kiaziz.transactionstatistics.transactioninfo.TransactionInfoService;

@RunWith(SpringRunner.class)
@SpringBootTest
//@ActiveProfiles("test")
//@TestPropertySource(locations = "classpath:application-test.properties")
public class TransactionInfoServiceJunitTest {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private long currentTimeinMillis;

	/**
	 * The application takes into consideration all transactions that were executed
	 * in the last 60 seconds
	 * 
	 */

	private final long cutoffDurationinMillis = 60000L;
	private long leastAllowedTransactionTimestamp;
	private long timeStampOlderThan60s;
	private long futureTimeStamp;
	private Long numberOfTransactionsWithCutoffTime=98L;
	
	@MockBean
	private TransactionInfoRepository repository;


	@Autowired
	private TestHelper testHelper;

	
	
	@Autowired
	private TransactionInfoService service;
	
	
	@Rule
	public TestRule watcher = new TestWatcher() {
	   protected void starting(Description description) {
	      logger.info("--------------------------------------------Starting test:--------------------------------------- :" + description.getMethodName());
	   }
	};
	

	@Before
	public void setup() {

		currentTimeinMillis = System.currentTimeMillis();
		leastAllowedTransactionTimestamp = currentTimeinMillis - cutoffDurationinMillis;
		timeStampOlderThan60s = leastAllowedTransactionTimestamp - cutoffDurationinMillis;
		futureTimeStamp=currentTimeinMillis+cutoffDurationinMillis;
	}
	
	@Test
	public void testGetStatistics()
	{
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
		given(repository.findAll()).willReturn(createTransactionInfoLists());
		DoubleSummaryStatistics statistics = service.getStatistics(now);
		assertThat(statistics.getCount(), is(numberOfTransactionsWithCutoffTime));
		
	}
	
	
	private List<TransactionInfo> createTransactionInfoLists()
	{
		List<TransactionInfo> transactionsList = new ArrayList<>();
		
		IntStream.range(0, 89).forEach(i -> {
			long timestamp=testHelper.generateRandomTimeStamp(timeStampOlderThan60s, leastAllowedTransactionTimestamp);
			ZonedDateTime transactionDateTime = ZonedDateTime
					.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
			transactionsList.add(new TransactionInfo(testHelper.generateRandomDouble(10, 100),
					transactionDateTime));
			
		});

		IntStream.range(0,numberOfTransactionsWithCutoffTime.intValue()).forEach(i -> {
			
			long timestamp=testHelper.generateRandomTimeStamp(leastAllowedTransactionTimestamp, currentTimeinMillis);
			ZonedDateTime transactionDateTime = ZonedDateTime
					.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
			transactionsList.add(new TransactionInfo(testHelper.generateRandomDouble(10, 100),
					transactionDateTime));
		});
		
		
		
		IntStream.range(0, 55).forEach(i -> {
			long timestamp=testHelper.generateRandomTimeStamp(currentTimeinMillis, futureTimeStamp);
			ZonedDateTime transactionDateTime = ZonedDateTime
					.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
			transactionsList.add(new TransactionInfo(testHelper.generateRandomDouble(10, 100),
					transactionDateTime));
			
		});
		return transactionsList;
	}	
	

}
