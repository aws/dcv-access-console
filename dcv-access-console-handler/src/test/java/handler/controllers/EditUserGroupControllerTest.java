// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.model.UserGroup;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EditUserGroupController.class)
public class EditUserGroupControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserGroupService mockUserGroupService;

    @MockBean
    private SessionTemplateService mockSessionTemplateService;

    @Value("${web-client-url}")
    private String origin;

    private final static String urlTemplate = "/editUserGroup";

    private final static String groupId = "group1";
    private final static String user1Id = "user1";
    private final static String user2Id = "user2";
    private final static String user3Id = "user3";
    private final static String user4Id = "user4";
    private final static String sessionTemplate1Id = "sessionTemplate1";
    private final static String sessionTemplate2Id = "sessionTemplate2";
    private final static String sessionTemplate3Id = "sessionTemplate3";
    private final static String sessionTemplate4Id = "sessionTemplate4";

    private final static List<String> userIdsToAdd = List.of(user1Id, user2Id);
    private final static List<String> userIdsToRemove = List.of(user3Id, user4Id);
    private final static List<String> sessionTemplateIdsToAdd = List.of(sessionTemplate1Id, sessionTemplate2Id);
    private final static List<String> sessionTemplateIdsToRemove = List.of(sessionTemplate3Id, sessionTemplate4Id);

    private final static String requestBody = "{\"UserGroupId\": \"group1\"}";
    private final static String fullRequestBody = "{" +
            "\"UserGroupId\": \"group1\"," +
            "\"UserIdsToAdd\": [\"user1\", \"user2\"]," +
            "\"UserIdsToRemove\": [\"user3\", \"user4\"]," +
            "\"SessionTemplateIdsToAdd\": [\"sessionTemplate1\", \"sessionTemplate2\"]," +
            "\"SessionTemplateIdsToRemove\": [\"sessionTemplate3\", \"sessionTemplate4\"]" +
        "}";

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
    public void testUnauthorized() throws Exception {
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInternalServerError() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.editUserGroup, ResourceType.Group, groupId)).thenThrow(new AuthorizationServiceException(""));

        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(requestBody))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testSuccess() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.editUserGroup, ResourceType.Group, groupId)).thenReturn(true);
        when(mockUserGroupService.editUserGroup(any())).thenReturn(new UserGroup().userGroupId(groupId));
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(fullRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.UserGroup", notNullValue()))
                .andExpect(jsonPath("$.UserGroup.UserGroupId", is(groupId)));

        ArgumentCaptor<String> usersRemoved = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usersAdded = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sessionTemplatesAdded = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sessionTemplatesRemoved = ArgumentCaptor.forClass(String.class);

        verify(mockAuthorizationEngine, times(2)).removeUserFromGroup(usersRemoved.capture(), any());
        verify(mockAuthorizationEngine, times(2)).addUserToGroup(usersAdded.capture(), any());
        verify(mockAuthorizationEngine, times(2)).removePrincipalFromSharedList(any(), any(),
                any(), sessionTemplatesRemoved.capture(), any());
        verify(mockAuthorizationEngine, times(2)).addPrincipalToSharedList(any(), any(),
                any(), sessionTemplatesAdded.capture(), any());

        assertThat(usersRemoved.getAllValues()).containsExactlyInAnyOrderElementsOf(userIdsToRemove);
        assertThat(usersAdded.getAllValues()).containsExactlyInAnyOrderElementsOf(userIdsToAdd);
        assertThat(sessionTemplatesRemoved.getAllValues()).containsExactlyInAnyOrderElementsOf(sessionTemplateIdsToRemove);
        assertThat(sessionTemplatesAdded.getAllValues()).containsExactlyInAnyOrderElementsOf(sessionTemplateIdsToAdd);
    }
}
