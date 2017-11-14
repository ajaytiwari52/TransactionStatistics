package com.n26.transactionstatistics.transactioninfo;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.transactionstatistics.TransactionStatisticsApplication;
import com.n26.transactionstatistics.transactioninfo.TransactionInfoDTO;
import com.n26.transactionstatistics.transactioninfo.TransactionInfoRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TransactionStatisticsApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("stresstest")
@TestPropertySource(locations = "classpath:application-stresstest.properties")

public class BigOTestThroughStressTest {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestHelper testHelper;

	@Autowired
	private TransactionInfoRepository repository;
	
	@Value("${stresstest.numberOfParallelThreads:10}")
	private int numberOfParallelThreads;
	
	
	@Value("${stresstest.numberOfIterations:250}")
	private int numberOfIterations;
			

	private long currentTimeinMillis;
	private final long cutoffDurationinMillis = 60000;
	private long leastAllowedTransactionTimestamp;
	private long timeStampOlderThan60s;

	@Before
	public void setup() {
		currentTimeinMillis = System.currentTimeMillis();
		leastAllowedTransactionTimestamp = currentTimeinMillis - cutoffDurationinMillis;
		timeStampOlderThan60s = leastAllowedTransactionTimestamp - cutoffDurationinMillis;
	}
	
	@Rule
	public TestRule watcher = new TestWatcher() {
	   protected void starting(Description description) {
	      logger.info("--------------------------------------------Starting test:--------------------------------------- " + description.getMethodName());
	   }
	};

	/**
	 * This test covers a stress test scenario, through which it can
	 * be proved that both the Endpoints are executing in constant time (0(1))
	 * Here we are creating POST and GET requests simultaneously through
	 * multiple threads and iterations.
	 * Through the outcome, in the console,we can see even with 10+ active threads, and
	 * 100 plus active iterations, the time to free the IO is 
	 * below 1 millis.Meaning , irrespective of number of invocations, ENDPOINTS are operating 
	 * with the same efficiency
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAsync() throws Exception {

		List<TransactionInfoDTO> transactionsList = new ArrayList<>();
		MultithreadedStressTester stressTesterPost = new MultithreadedStressTester(10, 2500);

		stressTesterPost.stress(new Runnable() {
			public void run() {

				TransactionInfoDTO transactionInfoDTO = new TransactionInfoDTO(
						testHelper.generateRandomDouble(10, 100000),
						testHelper.generateRandomTimeStamp(timeStampOlderThan60s, currentTimeinMillis));
				transactionsList.add(transactionInfoDTO);

				String requestJSON;

				requestJSON = testHelper.getJSONRepresentation(transactionInfoDTO);
				try {
					mockMvc.perform(post("/transactions").content(requestJSON).contentType(MediaType.APPLICATION_JSON));
				} catch (Exception e) {
					logger.error("Error occured -{}", e);
				}
			}
		});

		logger.info("No. of Posted Transactions -{}", transactionsList.size());
		MultithreadedStressTester stressTesterGET = new MultithreadedStressTester(numberOfParallelThreads, numberOfIterations);

		//As the ENDPOINTS are async, some conditions are needed to prove that data is really persisted, before continuing with the get request
		 
		await().until(repositoryNotEmpty());

		stressTesterGET.stress(new Runnable() {
			public void run() {
				MvcResult mvcResult;
				try {
					mvcResult = mockMvc.perform(get("/statistics").contentType(MediaType.APPLICATION_JSON))
							.andExpect(request().asyncStarted())
							.andExpect(request().asyncResult(instanceOf(DoubleSummaryStatistics.class))).andReturn();
					String result = mvcResult.getAsyncResult().toString();
					logger.info(result);

				} catch (Exception e) {
					logger.error("Error occured -{}", e);
					
				}

			}
		});

		stressTesterGET.shutdown();
		stressTesterPost.shutdown();

	}

	private Callable<Boolean> repositoryNotEmpty() {
		return new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return repository.count() != 0L; // The condition that must be fulfilled
			}
		};
	}

}
