package handler.controllers;

import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ValidateSessionTemplateController.class)
public class ValidateSessionTemplateControllerTest extends BaseControllerTest  {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BrokerClient mockBrokerClient;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/validateSessionTemplate";
    private final static String testString = "test";

    @Test
    public void testInternalServerException() throws Exception {
        doThrow(RuntimeException.class).when(mockBrokerClient).validateSessionTemplate(any(), anyBoolean());
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void validateSessionTemplateSuccess() throws Exception {
        Map<String, String> failureReasons = Map.of(testString, testString);
        doThrow(new BadRequestException("{\""+testString+"\":\""+testString+"\"}")).when(mockBrokerClient).validateSessionTemplate(any(), anyBoolean());
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FailureReasons", is(failureReasons)))
                .andExpect(jsonPath("$.Error", nullValue()));

        doNothing().when(mockBrokerClient).validateSessionTemplate(any(), anyBoolean());
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FailureReasons", is(new HashMap())))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
}
