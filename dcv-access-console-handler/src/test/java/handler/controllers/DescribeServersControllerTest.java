package handler.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import handler.brokerclients.BrokerClient;
import handler.config.AuthenticationConfig;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BrokerClientException;
import handler.model.DescribeServersUIRequestData;
import handler.model.DescribeServersUIResponse;
import handler.model.Server;
import handler.utils.Filter;
import handler.utils.Sort;

@WebMvcTest(DescribeServersController.class)
class DescribeServersControllerTest extends BaseControllerTest  {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private Filter<DescribeServersUIRequestData, Server> mockServerFilter;
    @MockBean
    private Sort<DescribeServersUIRequestData, Server> mockServerSort;
    @MockBean
    private BrokerClient mockBrokerClient;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/describeServers";
    private final static String testString = "testServer";

    @Test
    public void testBadRequest() throws Exception {
        when(mockBrokerClient.describeServers(any())).thenThrow(BadRequestException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBrokerAuthenticationException() throws Exception {
        when(mockBrokerClient.describeServers(any())).thenThrow(BrokerAuthenticationException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testBrokerClientException() throws Exception {
        when(mockBrokerClient.describeServers(any())).thenThrow(BrokerClientException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void describeServersSuccess() throws Exception {
        List<Server> servers = new ArrayList<>();
        Server testServer = new Server().id(testString);
        servers.add(testServer);
        when(mockBrokerClient.describeServers(any())).thenReturn(new DescribeServersUIResponse().servers(servers));
        when(mockServerFilter.getFiltered(any(), any())).thenReturn(servers);
        when(mockServerSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Servers", hasSize(1)))
                .andExpect(jsonPath("$.Servers[0].Id", is(testServer.getId())))
                .andExpect(jsonPath("$.Error", nullValue()));
    }

}
