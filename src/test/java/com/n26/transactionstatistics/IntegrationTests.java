package com.n26.transactionstatistics;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.n26.transactionstatistics.transactioninfo.MultithreadedStressTester;
import com.n26.transactionstatistics.transactioninfo.TestHelper;
import com.n26.transactionstatistics.transactioninfo.TransactionInfoDTO;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = TransactionStatisticsApplication.class)
@AutoConfigureMockMvc
// @TestPropertySource(
// locations = "classpath:application-integrationtest.properties")
@AutoConfigureTestDatabase
public class IntegrationTests {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TestHelper testHelper;
	
	private long currentTimeinMillis;
	private final long cutoffDurationinMillis=60000;
	private long leastAllowedTransactionTimestamp;
	private long timeStampOlderThan60s;
	
	
	
		@Before
		public void setup() {
			currentTimeinMillis = System.currentTimeMillis();
			leastAllowedTransactionTimestamp = currentTimeinMillis - cutoffDurationinMillis;
			timeStampOlderThan60s = leastAllowedTransactionTimestamp - cutoffDurationinMillis;
		}

	

	@Test
	public void testAsync() throws Exception
	{
		
		List<TransactionInfoDTO> transactionsList = new ArrayList<>();
		MultithreadedStressTester stressTester = new MultithreadedStressTester(10, 2500);
		
		
		 stressTester.stress(new Runnable() {
	            public void run() {
	            	
	            	TransactionInfoDTO transactionInfoDTO = new TransactionInfoDTO(testHelper.generateRandomDouble(10, 100),
	    					testHelper.generateRandomTimeStamp(timeStampOlderThan60s, currentTimeinMillis));
	            	transactionsList.add(transactionInfoDTO);
	            	
	            	String requestJSON;
					
						requestJSON = testHelper.getJSONRepresentation(transactionInfoDTO);
						try {
							mockMvc
							.perform(post("/transactions").content(requestJSON).contentType(MediaType.APPLICATION_JSON));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}               
	            }
	        });
		 
		 stressTester.shutdown();
		 
		 
		 logger.info("No. of Posted Transactions -{}",transactionsList.size());
		 
		 
/*		
		TransactionInfoDTO dto = new TransactionInfoDTO();
		dto.setAmount(12.09);
		dto.setTimestamp(System.currentTimeMillis());
		ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(dto);

		for (int i = 0; i < 100; i++) {
			MvcResult mvcResult = this.mockMvc
					.perform(post("/transactions").content(requestJson).contentType(MediaType.APPLICATION_JSON))

					.andExpect(request().asyncStarted())
					.andExpect(request().asyncResult(instanceOf(ResponseEntity.class))).andReturn();
			mvcResult.getAsyncResult();
			this.mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isCreated());
			

		}
		*/
	}

}
