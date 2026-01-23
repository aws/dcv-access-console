// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.SystemAction;
import handler.exceptions.BadRequestException;
import handler.services.SessionTemplateService;
import handler.services.UserService;
import handler.model.SessionTemplate;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeSessionTemplatesResponse;
import handler.model.DescribeUsersResponse;
import handler.model.User;
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
    @MockBean
    private UserService mockUserService;

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

    @Test
    public void describeSessionTemplatesWithLoginUsernameFilter() throws Exception {
        List<SessionTemplate> sessionTemplates = new ArrayList<>();
        SessionTemplate sessionTemplate = new SessionTemplate().id(testString).createdBy("uuid-123");
        sessionTemplates.add(sessionTemplate);

        // Mock user lookup: loginUsername "newuser" -> userId "uuid-123"
        when(mockUserService.describeUsers(any())).thenReturn(
            new DescribeUsersResponse().users(List.of(new User().userId("uuid-123").loginUsername("newuser")))
        );
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(
            new DescribeSessionTemplatesResponse().sessionTemplates(sessionTemplates).nextToken(null)
        );
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(sessionTemplates);
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, testString)).thenReturn(true);

        mvc.perform(
                post(urlTemplate)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ORIGIN, origin)
                    .content("{\"CreatedByLoginUsername\": [{\"Operator\": \"=\", \"Value\": \"newuser\"}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.SessionTemplates", hasSize(1)))
            .andExpect(jsonPath("$.SessionTemplates[0].Id", is(testString)));

        verify(mockUserService).describeUsers(any());
    }

    @Test
    public void describeSessionTemplatesWithNotContainsFilter_excludesMatchingTemplates() throws Exception {
        SessionTemplate includedTemplate = new SessionTemplate().id("included").createdBy("other-user");
        SessionTemplate excludedTemplate = new SessionTemplate().id("excluded").createdBy("uuid-admin");
        List<SessionTemplate> allTemplates = new ArrayList<>(List.of(includedTemplate, excludedTemplate));

        when(mockUserService.describeUsers(any())).thenReturn(
            new DescribeUsersResponse().users(List.of(new User().userId("uuid-admin").loginUsername("admin")))
        );
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(
            new DescribeSessionTemplatesResponse().sessionTemplates(allTemplates).nextToken(null)
        );
        // After preFilterExclusions, only includedTemplate remains
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(List.of(includedTemplate));
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, "included")).thenReturn(true);

        mvc.perform(
                post(urlTemplate)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ORIGIN, origin)
                    .content("{\"CreatedByLoginUsername\": [{\"Operator\": \"NOT_CONTAINS\", \"Value\": \"admin\"}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.SessionTemplates", hasSize(1)))
            .andExpect(jsonPath("$.SessionTemplates[0].Id", is("included")));
    }

    @Test
    public void describeSessionTemplatesWithContainsFilter_resolvesToMultipleUsers() throws Exception {
        SessionTemplate template1 = new SessionTemplate().id("t1").createdBy("uuid-admin1");
        SessionTemplate template2 = new SessionTemplate().id("t2").createdBy("uuid-admin2");
        List<SessionTemplate> templates = new ArrayList<>(List.of(template1, template2));

        // CONTAINS "admin" returns multiple users
        when(mockUserService.describeUsers(any())).thenReturn(
            new DescribeUsersResponse().users(List.of(
                new User().userId("uuid-admin1").loginUsername("admin1"),
                new User().userId("uuid-admin2").loginUsername("admin2")
            ))
        );
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(
            new DescribeSessionTemplatesResponse().sessionTemplates(templates).nextToken(null)
        );
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(templates);
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.isAuthorized(any(), any(), any(), any(), any())).thenReturn(true);

        mvc.perform(
                post(urlTemplate)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ORIGIN, origin)
                    .content("{\"CreatedByLoginUsername\": [{\"Operator\": \"CONTAINS\", \"Value\": \"admin\"}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.SessionTemplates", hasSize(2)));
    }

    @Test
    public void describeSessionTemplatesWithUsersSharedWithLoginUsername_batchesLookup() throws Exception {
        SessionTemplate template = new SessionTemplate().id(testString);
        List<SessionTemplate> templates = new ArrayList<>(List.of(template));

        when(mockUserService.describeUsers(any())).thenReturn(
            new DescribeUsersResponse().users(List.of(
                new User().userId("uuid-1").loginUsername("user1"),
                new User().userId("uuid-2").loginUsername("user2")
            ))
        );
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(
            new DescribeSessionTemplatesResponse().sessionTemplates(templates).nextToken(null)
        );
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(templates);
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.isAuthorized(any(), any(), (SystemAction) any())).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(any(), any(), any(), any(), any())).thenReturn(true);

        mvc.perform(
                post(urlTemplate)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ORIGIN, origin)
                    .content("{\"UsersSharedWithLoginUsername\": [{\"Operator\": \"=\", \"Value\": \"user1\"}, {\"Operator\": \"=\", \"Value\": \"user2\"}]}"))
            .andExpect(status().isOk());

        // Should make only 1 batched call, not 2 separate calls
        verify(mockUserService).describeUsers(any());
    }

    @Test
    public void describeSessionTemplatesWithCreatedByLoginUsername_batchesByOperatorType() throws Exception {
        SessionTemplate template = new SessionTemplate().id(testString).createdBy("uuid-1");
        List<SessionTemplate> templates = new ArrayList<>(List.of(template));

        // Mock returns user for both EQUAL and CONTAINS lookups
        when(mockUserService.describeUsers(any())).thenReturn(
            new DescribeUsersResponse().users(List.of(new User().userId("uuid-1").loginUsername("user1")))
        );
        when(mockSessionTemplateService.describeSessionTemplates(any())).thenReturn(
            new DescribeSessionTemplatesResponse().sessionTemplates(templates).nextToken(null)
        );
        when(mockSessionFilter.getFiltered(any(), any())).thenReturn(templates);
        when(mockSessionSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);
        when(mockAuthorizationEngine.isAuthorized(any(), any(), any(), any(), any())).thenReturn(true);

        // 2 EQUAL filters + 1 CONTAINS filter = max 2 DB calls (one per operator type)
        mvc.perform(
                post(urlTemplate)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ORIGIN, origin)
                    .content("{\"CreatedByLoginUsername\": [{\"Operator\": \"=\", \"Value\": \"user1\"}, {\"Operator\": \"=\", \"Value\": \"user2\"}, {\"Operator\": \"CONTAINS\", \"Value\": \"user\"}]}"))
            .andExpect(status().isOk());

        // Should make 2 batched calls (EQUAL bucket + CONTAINS bucket), not 3
        verify(mockUserService, org.mockito.Mockito.times(2)).describeUsers(any());
    }
}
