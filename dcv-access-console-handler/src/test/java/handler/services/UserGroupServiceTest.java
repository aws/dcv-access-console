// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.services;

import handler.exceptions.BadRequestException;
import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUserGroupsResponse;
import handler.model.EditUserGroupRequestData;
import handler.model.SortToken;
import handler.model.UserGroup;
import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.UserEntity;
import handler.persistence.UserGroupEntity;
import handler.persistence.UserGroupUser;
import handler.persistence.UserGroupUserMembership;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.UserGroupUserMembershipRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.Filter;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserGroupServiceTest {
    @InjectMocks
    private UserGroupService testUserGroupService;

    @Mock
    private PagingAndSortingCrudRepository<UserGroupEntity, String> mockUserGroupRepository;
    @Mock
    private UserGroupUserMembershipRepository mockUserGroupUserMembershipRepository;
    @Mock
    private SessionTemplateService mockSessionTemplateService;

    @Mock
    private Filter<DescribeUserGroupsRequestData, UserGroup> mockUserGroupFilter;


    private final static String testSortKey = "Name";

    private final static String GROUP1_ID = "userGroup1";
    private final static String GROUP2_ID = "userGroup2";
    private final static String GROUP3_ID = "userGroup3";
    private final static String GROUP4_ID = "userGroup4";
    private final static String GROUP5_ID = "userGroup5";

    private final static String GROUP1_NAME = "User Group 1";

    private final static String USER1_ID = "user1";
    private final static String USER2_ID = "user2";
    private final static String USER3_ID = "user3";
    private final static String USER4_ID = "user4";
    private final static String USER5_ID = "user5";
    private final static String USER6_ID = "user6";

    private final static String SESSION_TEMPLATE1_ID = "sessionTemplate1";
    private final static String SESSION_TEMPLATE2_ID = "sessionTemplate2";
    private final static String SESSION_TEMPLATE3_ID = "sessionTemplate3";
    private final static String SESSION_TEMPLATE4_ID = "sessionTemplate4";

    private final static List<String> GROUP_IDS = List.of(GROUP1_ID, GROUP2_ID, GROUP3_ID, GROUP4_ID, GROUP5_ID);
    private final static List<String> USER_IDS = List.of(USER1_ID, USER2_ID, USER3_ID, USER4_ID, USER5_ID, USER6_ID);

    private final static List<UserGroupEntity> USER_GROUP_ENTITIES = GROUP_IDS.stream().map(id -> (UserGroupEntity)(new UserGroupEntity().userGroupId(id)).isImported(false)).toList();
    private final static List<UserEntity> USER_ENTITIES = USER_IDS.stream().map(id -> (UserEntity)(new UserEntity().userId(id))).toList();

    @Test
    public void testDescribeUserGroupsBasic() {
        DescribeUserGroupsRequestData testRequest = new DescribeUserGroupsRequestData();
        testRequest.setSortToken(new SortToken().operator(SortToken.OperatorEnum.ASC).key(testSortKey));
        testRequest.setMaxResults(5);

        RepositoryResponse<UserGroupEntity> mockRepositoryResponse = mock(RepositoryResponse.class);
        when(mockRepositoryResponse.getItems()).thenReturn(USER_GROUP_ENTITIES);
        when(mockUserGroupRepository.findAll(any(RepositoryRequest.class))).thenReturn(mockRepositoryResponse);

        when(mockUserGroupFilter.getFiltered(any(), any())).thenReturn(USER_GROUP_ENTITIES.stream().map(group -> (UserGroup) group).toList());
        DescribeUserGroupsResponse response = testUserGroupService.describeUserGroups(testRequest);

        assertNotNull(response);
        assertEquals(5, response.getUserGroups().size());
        assertThat(response.getUserGroups()).containsExactlyInAnyOrderElementsOf(USER_GROUP_ENTITIES);
    }

    @Test
    public void testDescribeUserGroupsWithMemberships() {
        DescribeUserGroupsRequestData testRequest = new DescribeUserGroupsRequestData();
        testRequest.setSortToken(new SortToken().operator(SortToken.OperatorEnum.ASC).key(testSortKey));
        testRequest.setMaxResults(5);

        RepositoryResponse<UserGroupEntity> mockRepositoryResponse = mock(RepositoryResponse.class);
        when(mockRepositoryResponse.getItems()).thenReturn(USER_GROUP_ENTITIES);
        when(mockUserGroupRepository.findAll(any(RepositoryRequest.class))).thenReturn(mockRepositoryResponse);

        when(mockUserGroupFilter.getFiltered(any(), any())).thenReturn(USER_GROUP_ENTITIES.stream().map(group -> (UserGroup) group).toList());

        Map<String, List<UserGroupUserMembership>> memberships = new HashMap<>();
        for (int i = 0; i < USER_GROUP_ENTITIES.size(); i++) {
            if (!memberships.containsKey(GROUP_IDS.get(i))) {
                memberships.put(GROUP_IDS.get(i), new ArrayList<>());
            }
            // Add a different amount of users to each group
            for (int j = 0; j < i; j++) {
                memberships.get(GROUP_IDS.get(i)).add(new UserGroupUserMembership(USER_GROUP_ENTITIES.get(i), USER_ENTITIES.get(j)));
            }
        }

        doAnswer(invocation -> {
            String userGroupId = invocation.getArgument(0);
            return memberships.get(userGroupId);
        }).when(mockUserGroupUserMembershipRepository).findByUserGroupUserGroupId(any(String.class));

        DescribeUserGroupsResponse response = testUserGroupService.describeUserGroups(testRequest);
        assertNotNull(response);
        assertEquals(5, response.getUserGroups().size());
        assertThat(response.getUserGroups()).containsExactlyInAnyOrderElementsOf(USER_GROUP_ENTITIES);
        for (UserGroup group : response.getUserGroups()) {
            assertEquals(memberships.get(group.getUserGroupId()).size(), group.getUserIds().size());
        }
    }

    @Test
    public void testDescribeUsersBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> testUserGroupService.describeUserGroups(null));
    }
    @Test
    public void testDescribeUsersBadSortToken() {
        assertThrowsExactly(BadRequestException.class,
                () -> testUserGroupService.describeUserGroups(new DescribeUserGroupsRequestData().sortToken(new SortToken())));
    }

    @Test
    public void testCreateUserGroupDoesntExist() {
        when(mockUserGroupRepository.existsById(GROUP1_ID)).thenReturn(false);

        doAnswer(invocation -> {
            UserGroupEntity group = invocation.getArgument(0);
            assertEquals(GROUP1_ID, group.getUserGroupId());
            assertEquals(GROUP1_ID, group.getDisplayName());

            assertEquals(group.getCreationTime(), group.getLastModifiedTime());

            return group;
        }).when(mockUserGroupRepository).save(any());

        assertNotNull(testUserGroupService.createUserGroup(GROUP1_ID, false));
    }

    @Test
    public void testCreateUserGroupExists() {
        when(mockUserGroupRepository.existsById(GROUP1_ID)).thenReturn(true);

        verify(mockUserGroupRepository, never()).save(any());
        assertNull(testUserGroupService.createUserGroup(GROUP1_ID, false));
    }

    @Test
    public void testCreateUserGroupOrReturnIfExists_DoesntExist() {
        when(mockUserGroupRepository.findById(GROUP1_ID)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            UserGroupEntity group = invocation.getArgument(0);
            assertEquals(GROUP1_ID, group.getUserGroupId());
            assertEquals(GROUP1_NAME, group.getDisplayName());

            assertEquals(group.getCreationTime(), group.getLastModifiedTime());

            return group;
        }).when(mockUserGroupRepository).save(any());

        UserGroupEntity group = testUserGroupService.createUserGroupOrReturnIfExists(GROUP1_ID, GROUP1_NAME, false);

        assertNotNull(group);
        assertEquals(GROUP1_ID, group.getUserGroupId());
        assertEquals(GROUP1_NAME, group.getDisplayName());
        assertEquals(group.getCreationTime(), group.getLastModifiedTime());
    }

    @Test
    public void testCreateUserGroupOrReturnIfExists_GroupExists() {
        UserGroupEntity groupEntity = new UserGroupEntity();
        when(mockUserGroupRepository.findById(GROUP1_ID)).thenReturn(Optional.of(groupEntity));

        UserGroupEntity group = testUserGroupService.createUserGroupOrReturnIfExists(GROUP1_ID, GROUP1_NAME, false);

        assertNotNull(group);
        assertEquals(groupEntity, group);
    }

    @Test
    public void testAddUserToGroup() {
        UserEntity userEntity = (UserEntity) new UserEntity().userId(USER1_ID);
        UserGroupEntity userGroupEntity = (UserGroupEntity) new UserGroupEntity().userGroupId(GROUP1_ID);

        testUserGroupService.addUserToGroup(USER1_ID,  GROUP1_ID);
        verify(mockUserGroupUserMembershipRepository).save(new UserGroupUserMembership(userGroupEntity, userEntity));
    }

    @Test
    public void testGetUserGroupMemberships() {
        List<UserGroupUserMembership> userGroupUserMemberships = List.of(
                new UserGroupUserMembership(new UserGroupEntity(), new UserEntity())
        );
        when(mockUserGroupUserMembershipRepository.findAll()).thenReturn(userGroupUserMemberships);
        assertEquals(userGroupUserMemberships, testUserGroupService.getUserGroupUserMemberships());
    }

    @Test
    public void testEditUserGroupNotFound() {
        when(mockUserGroupRepository.findById(GROUP1_ID)).thenReturn(Optional.empty());
        assertThrowsExactly(BadRequestException.class, () -> testUserGroupService.editUserGroup(new EditUserGroupRequestData().userGroupId(GROUP1_ID)));
    }

    @Test
    public void testEditUserGroupSuccess() {
        when(mockUserGroupRepository.findById(GROUP1_ID)).thenReturn(Optional.of(USER_GROUP_ENTITIES.get(0)));
        EditUserGroupRequestData request = new EditUserGroupRequestData()
                .userGroupId(GROUP1_ID)
                .displayName(GROUP1_NAME)
                .userIdsToAdd(Arrays.asList(USER1_ID, USER2_ID, USER3_ID, null))
                .userIdsToRemove(Arrays.asList(USER4_ID, USER5_ID, null, USER6_ID))
                .sessionTemplateIdsToAdd(Arrays.asList(SESSION_TEMPLATE1_ID, null, SESSION_TEMPLATE2_ID))
                .sessionTemplateIdsToRemove(Arrays.asList(null, SESSION_TEMPLATE3_ID, SESSION_TEMPLATE4_ID));

        testUserGroupService.editUserGroup(request);

        ArgumentCaptor<UserGroupEntity> userGroupEntityArgumentCaptor = ArgumentCaptor.forClass(UserGroupEntity.class);
        verify(mockUserGroupRepository).save(userGroupEntityArgumentCaptor.capture());

        assertEquals(GROUP1_ID, userGroupEntityArgumentCaptor.getValue().getUserGroupId());
        assertEquals(GROUP1_NAME, userGroupEntityArgumentCaptor.getValue().getDisplayName());


        ArgumentCaptor<Iterable<UserGroupUser>> removedUserCaptor = ArgumentCaptor.forClass(Iterable.class);

        verify(mockUserGroupUserMembershipRepository).deleteAllById(removedUserCaptor.capture());
        assertNotNull(removedUserCaptor.getValue());
        List<String> usersRemoved = new ArrayList<>();
        removedUserCaptor.getValue().forEach(userGroupUser -> usersRemoved.add(userGroupUser.getUserId()));
        assertThat(usersRemoved).containsExactlyInAnyOrderElementsOf(List.of(USER4_ID, USER5_ID, USER6_ID));

        ArgumentCaptor<Iterable<UserGroupUserMembership>> addedUserCaptor = ArgumentCaptor.forClass(Iterable.class);

        verify(mockUserGroupUserMembershipRepository).saveAll(addedUserCaptor.capture());
        assertNotNull(addedUserCaptor.getValue());
        List<String> usersAdded = new ArrayList<>();
        addedUserCaptor.getValue().forEach(userGroupUser -> usersAdded.add(userGroupUser.getUser().getUserId()));
        assertThat(usersAdded).containsExactlyInAnyOrderElementsOf(List.of(USER1_ID, USER2_ID, USER3_ID));

        ArgumentCaptor<List<SessionTemplatePublishedToUserGroup>> unpublishedSessionTemplatesCaptor = ArgumentCaptor.forClass(List.class);

        verify(mockSessionTemplateService).removeUserGroupsFromSessionTemplate(unpublishedSessionTemplatesCaptor.capture());
        assertNotNull(unpublishedSessionTemplatesCaptor.getValue());
        List<String> sessionTemplatesUnpublished = new ArrayList<>();
        unpublishedSessionTemplatesCaptor.getValue().forEach(sessionTemplate -> sessionTemplatesUnpublished.add(sessionTemplate.getSessionTemplate().getId()));
        assertThat(sessionTemplatesUnpublished).containsExactlyInAnyOrderElementsOf(List.of(SESSION_TEMPLATE3_ID, SESSION_TEMPLATE4_ID));

        ArgumentCaptor<List<SessionTemplatePublishedToUserGroup>> publishedSessionTemplatesCaptor = ArgumentCaptor.forClass(List.class);

        verify(mockSessionTemplateService).publishSessionTemplatesToGroups(publishedSessionTemplatesCaptor.capture());
        assertNotNull(publishedSessionTemplatesCaptor.getValue());
        List<String> sessionTemplatesPublished = new ArrayList<>();
        publishedSessionTemplatesCaptor.getValue().forEach(sessionTemplate -> sessionTemplatesPublished.add(sessionTemplate.getSessionTemplate().getId()));
        assertThat(sessionTemplatesPublished).containsExactlyInAnyOrderElementsOf(List.of(SESSION_TEMPLATE1_ID, SESSION_TEMPLATE2_ID));
    }

    @Test
    public void testAddMembersToGroupEmpty() {
        assertFalse(testUserGroupService.addMembersToGroup(List.of()));
    }

    @Test
    public void testDeleteUserGroups() {
        List<String> groupIds = List.of(GROUP1_ID, GROUP2_ID);
        assertTrue(testUserGroupService.deleteUserGroups(groupIds));
        verify(mockUserGroupRepository).deleteAllById(groupIds);
    }
}
