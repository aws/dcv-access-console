package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BrokerClientException;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.DescribeSessionsUIResponse;
import handler.model.SessionWithPermissions;
import handler.utils.Filter;
import handler.utils.Sort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DescribeSessionsController.class)
class DescribeSessionsControllerTest extends BaseControllerTest  {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private Filter<DescribeSessionsUIRequestData, SessionWithPermissions> mockSessionFilter;
    @MockBean
    private Sort<DescribeSessionsUIRequestData, SessionWithPermissions> mockSessionSort;
    @MockBean
    private BrokerClient mockBrokerClient;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/describeSessions";
    private final static String testId = "testId";

    @Test
    public void testBadRequest() throws Exception {
        when(mockBrokerClient.describeSessions(any())).thenThrow(BadRequestException.class);
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
        when(mockBrokerClient.describeSessions(any())).thenThrow(BrokerAuthenticationException.class);
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
        when(mockBrokerClient.describeSessions(any())).thenThrow(BrokerClientException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void describeSessionsSuccess() throws Exception {
        List<SessionWithPermissions> sessions = new ArrayList<>();
        SessionWithPermissions testSession = new SessionWithPermissions().id(testId).owner(testUser);
        SessionWithPermissions failedSession = new SessionWithPermissions().id("fail").owner("fail");
        sessions.add(failedSession);
        sessions.add(testSession);
        when(mockBrokerClient.describeSessions(any())).thenReturn(new DescribeSessionsUIResponse().sessions(sessions));
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(sessions);
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("User");
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionDetails, ResourceType.Session, "fail")).thenReturn(false);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionDetails, ResourceType.Session, testId)).thenReturn(true);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Sessions", hasSize(1)))
                .andExpect(jsonPath("$.Sessions[0].Id", is(testId)))
                .andExpect(jsonPath("$.Sessions[0].LevelOfAccess", is("Owner")))
                .andExpect(jsonPath("$.Error", nullValue()));

        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionDetails, ResourceType.Session, "fail")).thenReturn(true);

        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Sessions", hasSize(2)))
                .andExpect(jsonPath("$.Sessions[0].Id", is("fail")))
                .andExpect(jsonPath("$.Sessions[0].LevelOfAccess", is("Admin")))
                .andExpect(jsonPath("$.Sessions[1].Id", is(testId)))
                .andExpect(jsonPath("$.Sessions[1].LevelOfAccess", is("Owner")))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
}
