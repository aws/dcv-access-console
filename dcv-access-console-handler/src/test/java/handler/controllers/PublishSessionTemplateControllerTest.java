package handler.controllers;

import handler.authorization.engines.entities.SetShareListResponse;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.services.SessionTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublishSessionTemplateController.class)
public class PublishSessionTemplateControllerTest extends BaseControllerTest  {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/publishSessionTemplate";
    private final static String testString = "test";

    @Test
    public void testBadRequest() throws Exception {
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnauthorizedException() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.publishSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(false);
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Id\": \"test\", \"UserIds\":  [\"test\"]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void publishSessionTemplateSuccess() throws Exception {
        List<String> userIds = Collections.singletonList(testUser);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.publishSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(true);
        when(mockAuthorizationEngine.setShareList(any(), any(), any(), any(), any())).thenReturn(SetShareListResponse.builder().successfulUsers(userIds).build());
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Id\": \"test\", \"UserIds\":  [\"test\"], \"GroupIds\":  [\"test\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulUsersList", hasSize(1)))
                .andExpect(jsonPath("$.SuccessfulUsersList[0]", is(testUser)))
                .andExpect(jsonPath("$.SuccessfulGroupsList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulGroupsList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulUsersList", nullValue()))
                .andExpect(jsonPath("$.Error", nullValue()));

        when(mockAuthorizationEngine.setShareList(any(), any(), any(), any(), any())).thenThrow(RuntimeException.class);
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Id\": \"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulUsersList", hasSize(0)))
                .andExpect(jsonPath("$.SuccessfulGroupsList", hasSize(0)))
                .andExpect(jsonPath("$.UnsuccessfulGroupsList", hasSize(0)))
                .andExpect(jsonPath("$.UnsuccessfulUsersList", hasSize(0)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
}
