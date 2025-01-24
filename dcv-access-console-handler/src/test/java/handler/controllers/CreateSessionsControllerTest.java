package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.SystemAction;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BrokerClientException;
import handler.model.CreateSessionsUIResponse;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeSessionTemplatesResponse;
import handler.model.SessionTemplate;
import handler.model.SessionWithPermissions;
import handler.services.SessionTemplateService;
import handler.utils.Filter;

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
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreateSessionsController.class)
public class CreateSessionsControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BrokerClient mockBrokerClient;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;

    @MockBean
    private Filter<DescribeSessionTemplatesRequestData, SessionTemplate> mockSessionTemplateFilter;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/createSessions";
    private final static String testString = "test";

    @Test
    public void testBadRequest() throws Exception {
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(new DescribeSessionTemplatesResponse().addSessionTemplatesItem(new SessionTemplate().id(testString)));
        when(mockSessionTemplateFilter.getFiltered(any(), any())).thenReturn(Collections.singletonList(new SessionTemplate().id(testString)));
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.useSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(true);
        when(mockBrokerClient.createSessions(any())).thenThrow(BadRequestException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBrokerAuthenticationException() throws Exception {
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(new DescribeSessionTemplatesResponse().addSessionTemplatesItem(new SessionTemplate().id(testString)));
        when(mockSessionTemplateFilter.getFiltered(any(), any())).thenReturn(Collections.singletonList(new SessionTemplate().id(testString)));
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.useSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(true);
        when(mockBrokerClient.createSessions(any())).thenThrow(BrokerAuthenticationException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testBrokerClientException() throws Exception {
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(new DescribeSessionTemplatesResponse().addSessionTemplatesItem(new SessionTemplate().id(testString)));
        when(mockSessionTemplateFilter.getFiltered(any(), any())).thenReturn(Collections.singletonList(new SessionTemplate().id(testString)));
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.useSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(true);
        when(mockBrokerClient.createSessions(any())).thenThrow(BrokerClientException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void createSessionsSuccess() throws Exception {
        SessionWithPermissions session = new SessionWithPermissions().id(testString);
        SessionWithPermissions deleteSession = new SessionWithPermissions().id("delete-session");
        SessionTemplate sessionTemplate = new SessionTemplate().id(testString);
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(new DescribeSessionTemplatesResponse().addSessionTemplatesItem(sessionTemplate));
        when(mockSessionTemplateFilter.getFiltered(any(), any())).thenReturn(Collections.singletonList(sessionTemplate));
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.useSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.useSpecificSessionTemplate, ResourceType.SessionTemplate, "delete-session")).thenReturn(true);
        when(mockBrokerClient.createSessions(any())).thenReturn(new CreateSessionsUIResponse().addSuccessfulListItem(session).addSuccessfulListItem(deleteSession));
        when(mockAuthorizationEngine.addSession(testString, testUser)).thenReturn(true);
        when(mockAuthorizationEngine.addSession("delete-session", testUser)).thenReturn(false);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{\"owner\": \"test-user\"}, {}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(2)))
                .andExpect(jsonPath("$.UnsuccessfulList", nullValue()))
                .andExpect(jsonPath("$.SuccessfulList[0].Id", is(testString)))
                .andExpect(jsonPath("$.SuccessfulList[1].Id", is("delete-session")))
                .andExpect(jsonPath("$.Error", nullValue()));
        verify(mockBrokerClient, times(1)).deleteSessions(any());

        when(mockBrokerClient.createSessions(any())).thenReturn(new CreateSessionsUIResponse().addSuccessfulListItem(session));
        when(mockAuthorizationEngine.addSession(any(), any())).thenReturn(true);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList", nullValue()))
                .andExpect(jsonPath("$.SuccessfulList[0].Id", is(testString)))
                .andExpect(jsonPath("$.Error", nullValue()));
        verify(mockBrokerClient, times(1)).deleteSessions(any());

        HashMap<String, String> errors = new HashMap<>();
        errors.put("Session Template", String.format("User %s is not authorized to use session template %s", testUser, sessionTemplate.getId()));
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.useSpecificSessionTemplate, ResourceType.SessionTemplate, sessionTemplate.getId())).thenReturn(false);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList[0].FailureReasons", is(errors)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }

    @Test
    public void createSessionsForOtherUserSuccess() throws Exception {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("Session Template", String.format("Session template %s not found", (Object) null));
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, SystemAction.createSessionsForOthers)).thenReturn(true);
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(new DescribeSessionTemplatesResponse().sessionTemplates(new ArrayList<>()));
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{\"Owner\": \"another-user\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList[0].FailureReasons", is(errors)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }

    @Test
    public void createSessionsForOtherUserUnauthorized() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, SystemAction.createSessionsForOthers)).thenReturn(false);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("[{\"Owner\": \"another-user\"}]"))
                .andExpect(status().isUnauthorized());
    }
}
