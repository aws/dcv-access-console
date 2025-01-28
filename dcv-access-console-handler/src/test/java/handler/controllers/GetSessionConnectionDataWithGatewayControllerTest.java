// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import static handler.controllers.GetSessionConnectionDataControllerTest.SESSION_ID;
import static handler.controllers.GetSessionConnectionDataControllerTest.TEST_AUTH_TOKEN;
import static handler.controllers.GetSessionConnectionDataControllerTest.TEST_HOST_PORT;
import static handler.controllers.GetSessionConnectionDataControllerTest.TEST_IP;
import static handler.controllers.GetSessionConnectionDataControllerTest.TEST_WEB_URL;
import static handler.controllers.GetSessionConnectionDataControllerTest.urlTemplate;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import handler.model.SessionWithPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.brokerclients.BrokerClient;
import handler.model.GetSessionConnectionDataUIResponse;
import handler.model.Server;
import handler.model.Session;

@WebMvcTest(GetSessionConnectionDataController.class)
@TestPropertySource(properties = {"enable-connection-gateway=true", "connection-gateway-host=testgatewayhostname",
        "connection-gateway-port=8444", "enable-public-ip-from-tag=false"})
public class GetSessionConnectionDataWithGatewayControllerTest extends BaseControllerTest {
    @Autowired
    protected MockMvc mvc;
    @MockBean
    protected BrokerClient mockBrokerClient;
    @Value("${web-client-url}")
    protected String origin;
    protected static final String TEST_GATEWAY = "testgatewayhostname";
    protected static final String TEST_GATEWAY_PORT = "8444";

    @Test
    public void getSessionConnectionDataSuccessWithGateway() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.connectToSession,
                ResourceType.Session, SESSION_ID)).thenReturn(true);
        when(mockBrokerClient.getSessionConnectionData(any(), any())).thenReturn(
                new GetSessionConnectionDataUIResponse().connectionToken(TEST_AUTH_TOKEN).session(
                        new SessionWithPermissions().id(SESSION_ID)
                                .server(new Server().ip(TEST_IP).webUrlPath(TEST_WEB_URL).port(TEST_HOST_PORT))));
        mvc.perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                        .header(HttpHeaders.ORIGIN, origin).content("{}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.ConnectionToken", is(TEST_AUTH_TOKEN))).andExpect(jsonPath("$.WebConnectionUrl",
                        is("https://%s:%s%s?authToken=%s#%s".formatted(TEST_GATEWAY, TEST_GATEWAY_PORT, TEST_WEB_URL,
                                TEST_AUTH_TOKEN, SESSION_ID)))).andExpect(jsonPath("$.NativeConnectionUrl",
                        is("dcv://%s:%s%s?authToken=%s#%s".formatted(TEST_GATEWAY, TEST_GATEWAY_PORT, TEST_WEB_URL,
                                TEST_AUTH_TOKEN, SESSION_ID))));
    }
}
