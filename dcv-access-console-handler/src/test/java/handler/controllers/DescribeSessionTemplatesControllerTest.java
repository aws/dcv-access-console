package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.SystemAction;
import handler.exceptions.BadRequestException;
import handler.services.SessionTemplateService;
import handler.model.SessionTemplate;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeSessionTemplatesResponse;
import handler.utils.Filter;
import handler.utils.Sort;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DescribeSessionTemplatesController.class)
public class DescribeSessionTemplatesControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;
    @MockBean
    private Filter<DescribeSessionTemplatesRequestData, SessionTemplate> mockSessionFilter;
    @MockBean
    private Sort<DescribeSessionTemplatesRequestData, SessionTemplate> mockSessionSort;

    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/describeSessionTemplates";
    private final static String testString = "test";
    private final static String failString = "fail";

    @Test
    public void testBadRequest() throws Exception {
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenThrow(BadRequestException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInternalServerException() throws Exception {
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenThrow(RuntimeException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void describeSessionTemplatesSuccess() throws Exception {
        List<SessionTemplate> sessionTemplates = new ArrayList<>();
        SessionTemplate sessionTemplate = new SessionTemplate().id(testString);
        SessionTemplate failedTemplate = new SessionTemplate().id(failString);
        sessionTemplates.add(failedTemplate);
        sessionTemplates.add(sessionTemplate);
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(new DescribeSessionTemplatesResponse().sessionTemplates(sessionTemplates).nextToken(null));
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(sessionTemplates);
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, failString)).thenReturn(false);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, testString)).thenReturn(true);

        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SessionTemplates", hasSize(1)))
                .andExpect(jsonPath("$.SessionTemplates[0].Id", is(testString)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
    @Test
    public void describeSessionTemplatesFilterByGroupId() throws Exception {
        List<SessionTemplate> sessionTemplates = new ArrayList<>();
        SessionTemplate sessionTemplate = new SessionTemplate().id(testString);
        sessionTemplates.add(sessionTemplate);
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(new DescribeSessionTemplatesResponse().sessionTemplates(sessionTemplates).nextToken(null));
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(sessionTemplates);
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, "user1", ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, testString)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, SystemAction.describeSessionTemplatesForOthers)).thenReturn(true);

        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"UsersSharedWith\": [{\"Operator\": \"=\", \"Value\": \"group1\"}], \"UserId\": \"user1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SessionTemplates", hasSize(1)))
                .andExpect(jsonPath("$.SessionTemplates[0].Id", is(testString)))
                .andExpect(jsonPath("$.Error", nullValue()));

        verify(mockSessionTemplateService).filterByUserId(any(), any());
        verify(mockSessionTemplateService).filterByGroupId(any(), any());
    }
}
