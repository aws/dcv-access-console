// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.model.SessionTemplate;
import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.UserGroupEntity;
import handler.persistence.UserGroupUser;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeleteUserGroupsController.class)
public class DeleteUserGroupsControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserGroupService mockUserGroupService;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;

    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/deleteUserGroups";
    private final static String userGroupId1 = "userGroupId1";
    private final static String userGroupId2 = "userGroupId2";
    private final static String userGroupId3 = "userGroupId3";
    private final static String deleteUserGroupsRequestDeleteIfNotEmpty = "{\"UserGroupIds\": [\"userGroupId1\", \"userGroupId2\", \"userGroupId3\"], \"DeleteIfNotEmpty\": true}";
    private final static String deleteUserGroupsRequestDontDeleteIfNotEmpty = "{\"UserGroupIds\": [\"userGroupId1\", \"userGroupId2\"], \"DeleteIfNotEmpty\": false}";
    private final static List<String> userIds = List.of("userId1", "userId2");

    private final static SessionTemplate sessionTemplate1 = new SessionTemplate().id("sessionTemplate1");
    private final static SessionTemplate sessionTemplate2 = new SessionTemplate().id("sessionTemplate2");

    private final static UserGroupEntity userGroupEntity1 = (UserGroupEntity) new UserGroupEntity().userGroupId(userGroupId1);
    private final static UserGroupEntity userGroupEntity2 = (UserGroupEntity) new UserGroupEntity().userGroupId(userGroupId2);

    private final static List<SessionTemplatePublishedToUserGroup> sessionTemplatesPublished = List.of(
            new SessionTemplatePublishedToUserGroup(sessionTemplate1, userGroupEntity1),
            new SessionTemplatePublishedToUserGroup(sessionTemplate2, userGroupEntity1)
    );

    @Test
    public void testBadRequest() throws Exception {
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"UserGroupIds\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInternalServerException() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId1)).thenReturn(true);
        when(mockUserGroupService.getUserIdsForGroup(any())).thenThrow(RuntimeException.class);
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(deleteUserGroupsRequestDeleteIfNotEmpty))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testDeleteUserGroupsCantDeleteNonEmptyGroup() throws Exception {

        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId1)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId2)).thenReturn(true);

        when(mockUserGroupService.getUserIdsForGroup(userGroupId1)).thenReturn(userIds);
        when(mockUserGroupService.getUserIdsForGroup(userGroupId2)).thenReturn(Collections.emptyList());

        when(mockSessionTemplateService.getSessionTemplatesPublishedToUserGroup(userGroupId2)).thenReturn(Collections.emptyList());
        when(mockAuthorizationEngine.removeGroup(userGroupId2)).thenReturn(true);

        doAnswer(invocation -> {
            List<String> userGroupIds = invocation.getArgument(0);
            assertEquals(1, userGroupIds.size());
            assertEquals(userGroupId2, userGroupIds.get(0));

            return true;
        }).when(mockUserGroupService).deleteUserGroups(anyList());

        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(deleteUserGroupsRequestDontDeleteIfNotEmpty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.SuccessfulList[0]", is(userGroupId2)))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList[0]", is(userGroupId1)));
    }

    @Test
    public void testDeleteUserGroupsSuccess() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId1)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId2)).thenReturn(false);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId3)).thenReturn(true);

        when(mockUserGroupService.getUserIdsForGroup(userGroupId1)).thenReturn(userIds);
        when(mockUserGroupService.getUserIdsForGroup(userGroupId3)).thenReturn(Collections.emptyList());
        when(mockSessionTemplateService.getSessionTemplatesPublishedToUserGroup(userGroupId1)).thenReturn(sessionTemplatesPublished);
        when(mockSessionTemplateService.getSessionTemplatesPublishedToUserGroup(userGroupId3)).thenReturn(Collections.emptyList());
        when(mockAuthorizationEngine.removeGroup(userGroupId1)).thenReturn(true);
        when(mockAuthorizationEngine.removeGroup(userGroupId3)).thenReturn(false);

        List<String> userIdsDeleted = new ArrayList<>();
        doAnswer(invocation -> {
            String userId = invocation.getArgument(0);
            String groupId = invocation.getArgument(1);

            assertEquals(userGroupId1, groupId);
            userIdsDeleted.add(userId);

            return true;
        }).when(mockAuthorizationEngine).removeUserFromGroup(any(), any());

        doAnswer(invocation -> {
            List<UserGroupUser> members = invocation.getArgument(0);
            assertEquals(2, members.size());

            return true;
        }).when(mockUserGroupService).removeMembersFromGroup(anyList());

        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(deleteUserGroupsRequestDeleteIfNotEmpty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Error", nullValue()))
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.SuccessfulList[0]", is(userGroupId1)))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(2)))
                .andExpect(jsonPath("$.UnsuccessfulList[0]", is(userGroupId2)))
                .andExpect(jsonPath("$.UnsuccessfulList[1]", is(userGroupId3)));

        assertThat(userIdsDeleted).containsExactlyInAnyOrderElementsOf(userIds);
        verify(mockAuthorizationEngine, times(1)).removePrincipalFromSharedList(PrincipalType.Group, userGroupId1, ResourceType.SessionTemplate, sessionTemplate1.getId(), ShareLevel.publishedTo);
        verify(mockAuthorizationEngine, times(1)).removePrincipalFromSharedList(PrincipalType.Group, userGroupId1, ResourceType.SessionTemplate, sessionTemplate2.getId(), ShareLevel.publishedTo);

        verify(mockSessionTemplateService).removeUserGroupsFromSessionTemplate(sessionTemplatesPublished);
    }

    @Test
    public void testDeleteUserGroupsEmpty() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId1)).thenReturn(false);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId2)).thenReturn(false);

        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(deleteUserGroupsRequestDontDeleteIfNotEmpty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Error", nullValue()))
                .andExpect(jsonPath("$.SuccessfulList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(2)))
                .andExpect(jsonPath("$.UnsuccessfulList[0]", is(userGroupId1)))
                .andExpect(jsonPath("$.UnsuccessfulList[1]", is(userGroupId2)));
    }
}
