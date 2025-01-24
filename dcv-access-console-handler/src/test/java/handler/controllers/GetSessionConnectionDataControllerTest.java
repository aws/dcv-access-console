package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BrokerClientException;
import handler.model.GetSessionConnectionDataUIResponse;
import handler.model.KeyValuePair;
import handler.model.Server;
import handler.model.Session;

import handler.model.SessionWithPermissions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

@WebMvcTest(GetSessionConnectionDataController.class)
public class GetSessionConnectionDataControllerTest extends BaseControllerTest {
    @Autowired
    protected MockMvc mvc;
    @MockBean
    protected BrokerClient mockBrokerClient;
    @Value("${web-client-url}")
    protected String origin;
    protected final static String SESSION_ID = "test-session";
    protected final static String urlTemplate = "/getSessionConnectionData/" + SESSION_ID;
    protected final static String TEST_WEB_URL = "/";
    protected final static String TEST_AUTH_TOKEN = "EXAMPLE.asdf";
    protected static final String TEST_IP = "127.0.0.1";
    protected static final String TEST_PUBLIC_IP = "54.239.28.85";
    protected static final String TEST_HOST_PORT = "8445";

    @Test
    public void testBadRequest() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.connectToSession,
                ResourceType.Session, SESSION_ID)).thenReturn(true);
        when(mockBrokerClient.getSessionConnectionData(any(), any())).thenThrow(BadRequestException.class);
        mvc.perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                .header(HttpHeaders.ORIGIN, origin).content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    public void testBrokerAuthenticationException() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.connectToSession, ResourceType.Session, SESSION_ID)).thenReturn(true);
        when(mockBrokerClient.getSessionConnectionData(any(), any())).thenThrow(BrokerAuthenticationException.class);
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
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.connectToSession, ResourceType.Session, SESSION_ID)).thenReturn(true);
        when(mockBrokerClient.getSessionConnectionData(any(), any())).thenThrow(BrokerClientException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getSessionConnectionDataSuccess() throws Exception {
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
                        is("https://%s:%s%s?authToken=%s#%s".formatted(TEST_IP, TEST_HOST_PORT, TEST_WEB_URL, TEST_AUTH_TOKEN,
                                SESSION_ID)))).andExpect(jsonPath("$.NativeConnectionUrl",
                        is("dcv://%s:%s%s?authToken=%s#%s".formatted(TEST_IP, TEST_HOST_PORT, TEST_WEB_URL, TEST_AUTH_TOKEN,
                                SESSION_ID))));
    }

    @Test
    public void getSessionConnectionDataSuccessWithTag() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.connectToSession,
                ResourceType.Session, SESSION_ID)).thenReturn(true);
        when(mockBrokerClient.getSessionConnectionData(any(), any())).thenReturn(
                new GetSessionConnectionDataUIResponse().connectionToken(TEST_AUTH_TOKEN).session(
                        new SessionWithPermissions().id(SESSION_ID).server(new Server().ip(TEST_IP)
                                .tags(Arrays.asList(new KeyValuePair().key("some").value("thing"),
                                        new KeyValuePair().key("public_ipv4").value(TEST_PUBLIC_IP)))
                                .webUrlPath(TEST_WEB_URL).port(TEST_HOST_PORT))));
        mvc.perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                        .header(HttpHeaders.ORIGIN, origin).content("{}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.ConnectionToken", is(TEST_AUTH_TOKEN))).andExpect(jsonPath("$.WebConnectionUrl",
                        is("https://%s:%s%s?authToken=%s#%s".formatted(TEST_PUBLIC_IP, TEST_HOST_PORT, TEST_WEB_URL,
                                TEST_AUTH_TOKEN, SESSION_ID)))).andExpect(jsonPath("$.NativeConnectionUrl",
                        is("dcv://%s:%s%s?authToken=%s#%s".formatted(TEST_PUBLIC_IP, TEST_HOST_PORT, TEST_WEB_URL,
                                TEST_AUTH_TOKEN, SESSION_ID))));
    }

    @Test
    public void testAuthorizationException() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.connectToSession,
                ResourceType.Session, SESSION_ID)).thenReturn(false);
        when(mockBrokerClient.getSessionConnectionData(any(), any())).thenThrow(BrokerClientException.class);
        mvc.perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                .header(HttpHeaders.ORIGIN, origin).content("{}")).andExpect(status().isUnauthorized());
    }
}
