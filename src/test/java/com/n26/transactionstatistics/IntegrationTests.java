package com.n26.transactionstatistics;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.n26.transactionstatistics.transactioninfo.TransactionInfoDTO;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = TransactionStatisticsApplication.class)
@AutoConfigureMockMvc
// @TestPropertySource(
// locations = "classpath:application-integrationtest.properties")
@AutoConfigureTestDatabase
public class IntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testAsync() throws Exception

	{
		TransactionInfoDTO dto = new TransactionInfoDTO();
		dto.setAmount(12.09);
		dto.setTimestamp(System.currentTimeMillis());
		ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
		String requestJson = ow.writeValueAsString(dto);

		for (int i = 0; i < 1000; i++) {
			MvcResult mvcResult = this.mockMvc
					.perform(post("/transactions").content(requestJson).contentType(MediaType.APPLICATION_JSON))

					.andExpect(request().asyncStarted())
					.andExpect(request().asyncResult(instanceOf(ResponseEntity.class))).andReturn();
			mvcResult.getAsyncResult();
			this.mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isCreated());

		}

	}

}
