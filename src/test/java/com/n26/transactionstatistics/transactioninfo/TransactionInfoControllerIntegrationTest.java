package com.n26.transactionstatistics.transactioninfo;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.n26.transactionstatistics.TransactionStatisticsApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TransactionStatisticsApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class TransactionInfoControllerIntegrationTest {

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

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TestHelper testHelper;

	@Autowired
	private TransactionInfoRepository repository;

	@Before
	public void setup() {
		currentTimeinMillis = System.currentTimeMillis();
		leastAllowedTransactionTimestamp = currentTimeinMillis - cutoffDurationinMillis;
		timeStampOlderThan60s = leastAllowedTransactionTimestamp - cutoffDurationinMillis;
	}
	
	@Rule
	public TestRule watcher = new TestWatcher() {
	   protected void starting(Description description) {
	      logger.info("--------------------------------------------Starting test:--------------------------------------- :" + description.getMethodName());
	   }
	};

	/*
	 * Here we are doing positive test. Meaning all the transactions are younger
	 * 60s, Firstly we are posting transactions, where all the timestamps are within
	 * 60s of currentTimeinMillis Along with that we are also testing
	 * asynchronousness of our end points.Meaning that the endpoints are
	 * non-blocking
	 */
	@Test
	public void testAllTransactionsAreYoungerThan60s() throws Exception {

		List<TransactionInfoDTO> transactionsList = new ArrayList<>();

		IntStream.range(0, 5).forEach(i -> {
			transactionsList.add(new TransactionInfoDTO(testHelper.generateRandomDouble(10, 100),
					testHelper.generateRandomTimeStamp(leastAllowedTransactionTimestamp, currentTimeinMillis)));
		});

		IntStream.range(0, transactionsList.size()).forEach(i -> {

			ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
			String requestJson;
			try {
				requestJson = ow.writeValueAsString(transactionsList.get(i));
				MvcResult mvcResult = this.mockMvc
						.perform(post("/transactions").content(requestJson).contentType(MediaType.APPLICATION_JSON))

						.andExpect(request().asyncStarted()).andExpect(status().is(200))
						.andExpect(request().asyncResult(instanceOf(ResponseEntity.class))).andReturn();
				mvcResult.getAsyncResult();
				this.mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isCreated());
			} catch (Exception e) {
				logger.error("Error occured -{}", e);
			}

		});

		MvcResult mvcResult = this.mockMvc.perform(get("/statistics").contentType(MediaType.APPLICATION_JSON))
				.andExpect(request().asyncStarted())
				.andExpect(request().asyncResult(instanceOf(DoubleSummaryStatistics.class))).andReturn();
		mvcResult.getAsyncResult();
		this.mockMvc.perform(asyncDispatch(mvcResult)).andExpect(jsonPath("count").value(transactionsList.size()));

	}

	/*
	 * Here we are doing negative test. Meaning all the transactions are older than
	 * 60s, Firstly we are posting transactions, where all the timestamps are at
	 * least 60s older thatn currentTimeinMillis Along with that we are also testing
	 * asynchronousness of our end points.Meaning that the endpoints are
	 * non-blocking
	 */

	@Test
	public void testAllTransactionsAreOlderThan60s() throws Exception {

		List<TransactionInfoDTO> transactionsList = new ArrayList<>();

		IntStream.range(0, 5).forEach(i -> {
			transactionsList.add(new TransactionInfoDTO(testHelper.generateRandomDouble(10, 100),
					testHelper.generateRandomTimeStamp(timeStampOlderThan60s, leastAllowedTransactionTimestamp)));
		});

		IntStream.range(0, transactionsList.size()).forEach(i -> {

			ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
			String requestJson;
			try {
				requestJson = ow.writeValueAsString(transactionsList.get(i));
				this.mockMvc.perform(post("/transactions").content(requestJson).contentType(MediaType.APPLICATION_JSON))
						.andExpect(request().asyncNotStarted()).andExpect(status().isNoContent()).andReturn();
			} catch (Exception e) {
				logger.error("Error occured -{}", e);
			}

		});

		MvcResult mvcResult = this.mockMvc.perform(get("/statistics").contentType(MediaType.APPLICATION_JSON))
				.andExpect(request().asyncStarted())
				.andExpect(request().asyncResult(instanceOf(DoubleSummaryStatistics.class))).andReturn();
		mvcResult.getAsyncResult();
		this.mockMvc.perform(asyncDispatch(mvcResult)).andExpect(jsonPath("count").value(0));

	}

	/*
	 * Here we are doing a mixed bag test(positive and negative). In this scenario we have both
	 * transactions that are older than and younger than 60s. After that we are
	 * asserting that, only the transactions that are younger than 60s are returned
	 * from the statistics API Along with that we are also testing asynchronousness
	 * of our end points.
	 */

	@Test
	public void testMixedBag() throws Exception {
		List<TransactionInfoDTO> transactionsList = new ArrayList<>();
		

		IntStream.range(0, 10).forEach(i -> {
			transactionsList.add(new TransactionInfoDTO(testHelper.generateRandomDouble(10, 100),
					testHelper.generateRandomTimeStamp(timeStampOlderThan60s, currentTimeinMillis)));
		});

		int numberOfValidTransactions = getListOfTransactionYoungerThan60s(transactionsList).size();

		IntStream.range(0, transactionsList.size()).forEach(i -> {

			ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
			String requestJson;
			try {
				requestJson = ow.writeValueAsString(transactionsList.get(i));
				this.mockMvc.perform(post("/transactions").content(requestJson).contentType(MediaType.APPLICATION_JSON))
						.andReturn();

			} catch (Exception e) {
				logger.error("Error occured -{}", e);
			}

		});

		logger.error("getListOfTransactionYoungerThan60s size -{}", numberOfValidTransactions);
		// As the ENDPOINTS are async, some conditions are needed to prove that data is
		// really persisted, before continuing with the get request
		await().until(repositoryNotEmpty());

		MvcResult mvcResult = this.mockMvc.perform(get("/statistics").contentType(MediaType.APPLICATION_JSON))
				.andExpect(request().asyncStarted())
				.andExpect(request().asyncResult(instanceOf(DoubleSummaryStatistics.class))).andReturn();
		mvcResult.getAsyncResult();
		this.mockMvc.perform(asyncDispatch(mvcResult)).andExpect(jsonPath("count").value(numberOfValidTransactions));

	}

	private List<TransactionInfoDTO> getListOfTransactionYoungerThan60s(List<TransactionInfoDTO> transactionsList) {
		List<TransactionInfoDTO> list = transactionsList.stream()
				.filter(p -> p.getTimestamp().longValue() > leastAllowedTransactionTimestamp)
				.collect(Collectors.toList());
		return list;

	}

	private Callable<Boolean> repositoryNotEmpty() {
		return new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return repository.count() != 0L; // The condition that must be fulfilled
			}
		};
	}

}
