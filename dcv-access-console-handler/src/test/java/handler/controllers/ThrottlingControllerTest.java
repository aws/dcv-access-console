// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import static handler.throttling.ThrottlingInterceptor.THROTTLE_MESSAGE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import handler.model.DescribeServersUIRequestData;
import handler.model.Server;
import handler.services.SessionTemplateService;
import handler.utils.Filter;
import handler.utils.Sort;

@WebMvcTest(DescribeServersController.class)
public class ThrottlingControllerTest extends BaseControllerTest {
    final static private String TEST_PATH = "/anyPath";

    @Autowired
    private MockMvc mvc;

    @Value("${web-client-url}")
    private String origin;

    @MockBean
    private BrokerClient mockBrokerClient;

    @MockBean
    private Sort<DescribeServersUIRequestData, Server> mockServerSort;

    @MockBean
    private SessionTemplateService mockSessionTemplateService;

    @MockBean
    private Filter<DescribeServersUIRequestData, Server> mockServerFilter;


    @Test
    public void testThrottling() throws Exception {
        when(this.mockProbe.isConsumed()).thenReturn(false);
            mvc.perform(
                            post(TEST_PATH)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                    .header(HttpHeaders.ORIGIN, origin)
                                    .content("{}"))
                    .andExpect(status().isTooManyRequests()).andExpect(status().reason(THROTTLE_MESSAGE));
        }
}
