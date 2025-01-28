// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BrokerClientException;
import handler.model.DeleteSessionsUIResponse;
import handler.model.SuccessfulDeleteSessionResponse;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeleteSessionsController.class)

public class DeleteSessionsControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BrokerClient mockBrokerClient;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/deleteSessions";
    private final static String SESSION_ID = "sessionId";


    @Test
    public void testBadRequest() throws Exception {
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockBrokerClient.deleteSessions(any())).thenThrow(BadRequestException.class);
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBrokerAuthenticationException() throws Exception {
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockBrokerClient.deleteSessions(any())).thenThrow(BrokerAuthenticationException.class);
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testBrokerClientException() throws Exception {
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockBrokerClient.deleteSessions(any())).thenThrow(BrokerClientException.class);
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void deleteSessionsSuccess() throws Exception {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("Unauthorized", String.format("User %s is not authorized to delete session %s", testUser, "fail"));
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("User");
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteSession, ResourceType.Session, SESSION_ID)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteSession, ResourceType.Session, "fail")).thenReturn(false);
        when(mockBrokerClient.deleteSessions(any())).thenReturn(new DeleteSessionsUIResponse().addSuccessfulListItem(new SuccessfulDeleteSessionResponse().sessionId(SESSION_ID)));
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{\"SessionId\": \"sessionId\", \"Owner\":  \"test\"}, {\"SessionId\": \"fail\", \"Owner\":  \"test\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.SuccessfulList[0].SessionId", is(SESSION_ID)))
                .andExpect(jsonPath("$.UnsuccessfulList[0].SessionId", is("fail")))
                .andExpect(jsonPath("$.UnsuccessfulList[0].FailureReasons", is(errors)))
                .andExpect(jsonPath("$.Error", nullValue()));

        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockBrokerClient.deleteSessions(any())).thenReturn(new DeleteSessionsUIResponse().addSuccessfulListItem(new SuccessfulDeleteSessionResponse().sessionId(SESSION_ID)));
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{\"SessionId\": \"sessionId\", \"Owner\":  \"test\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList", nullValue()))
                .andExpect(jsonPath("$.SuccessfulList[0].SessionId", is(SESSION_ID)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
}
