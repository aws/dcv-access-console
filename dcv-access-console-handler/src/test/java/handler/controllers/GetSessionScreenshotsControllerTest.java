package handler.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.WebContentGenerator;

import handler.brokerclients.BrokerClient;
import handler.model.GetSessionScreenshotsUIResponse;
import handler.model.GetSessionScreenshotSuccessfulResponse;
import handler.model.SessionScreenshot;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BrokerClientException;

@WebMvcTest(GetSessionScreenshotsController.class)
class GetSessionScreenshotsControllerTest extends BaseControllerTest  {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BrokerClient mockBrokerClient;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/getSessionScreenshots";
    private final static String sessionId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private final static String badSessionId = "00000000-1111-2222-3333-444444444444";

    @Test
    public void testBadRequest() throws Exception {
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"SessionIds\":  null}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void testSessionId() throws Exception {
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"SessionIds\":  [\"test\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList[0].FailureReason", is("Invalid Session ID")))
                .andExpect(jsonPath("$.Error", nullValue()));
    }

    @Test
    public void testBrokerAuthenticationException() throws Exception {
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockBrokerClient.getSessionScreenshots(any())).thenThrow(BrokerAuthenticationException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"SessionIds\":  [\"" + sessionId + "\"]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testBrokerClientException() throws Exception {
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockBrokerClient.getSessionScreenshots(any())).thenThrow(BrokerClientException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"SessionIds\":  [\"" + sessionId + "\"]}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetSessionScreenshotsSuccess() throws Exception {
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("User");
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionScreenshotDetails, ResourceType.Session, sessionId)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionScreenshotDetails, ResourceType.Session, badSessionId)).thenReturn(false);
        SessionScreenshot testSessionScreenshot = new SessionScreenshot().sessionId(sessionId);
        when(mockBrokerClient.getSessionScreenshots(any())).thenReturn(new GetSessionScreenshotsUIResponse().addSuccessfulListItem(new GetSessionScreenshotSuccessfulResponse().sessionScreenshot(testSessionScreenshot)));
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"SessionIds\":  [\"" + sessionId + "\", \"" + badSessionId + "\", \"test\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(2)))
                .andExpect(jsonPath("$.SuccessfulList[0].SessionScreenshot.SessionId", is(sessionId)))
                .andExpect(jsonPath("$.UnsuccessfulList[0].FailureReason", is(String.format("User %s is not authorized to view session screenshot details for session %s", testUser, badSessionId))))
                .andExpect(jsonPath("$.UnsuccessfulList[1].FailureReason", is("Invalid Session ID")))
                .andExpect(jsonPath("$.UnsuccessfulList[0].GetSessionScreenshotRequestData.SessionId", is(badSessionId)))
                .andExpect(jsonPath("$.Error", nullValue()));

        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn("Admin");
        when(mockBrokerClient.getSessionScreenshots(any())).thenReturn(new GetSessionScreenshotsUIResponse().addSuccessfulListItem(new GetSessionScreenshotSuccessfulResponse().sessionScreenshot(testSessionScreenshot)));
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"SessionIds\":  [\"" + sessionId + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList", nullValue()))
                .andExpect(jsonPath("$.SuccessfulList[0].SessionScreenshot.SessionId", is(sessionId)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
}
