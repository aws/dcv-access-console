package handler.services;

import handler.exceptions.BadRequestException;
import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUserGroupsResponse;
import handler.model.DescribeUsersRequestData;
import handler.model.DescribeUsersResponse;
import handler.model.FilterToken;
import handler.model.FilterTokenStrict;
import handler.model.ImportUsersResponse;
import handler.model.SortToken;
import handler.model.User;
import handler.model.UserGroup;
import handler.persistence.UserEntity;
import handler.persistence.UserGroupEntity;
import handler.persistence.UserGroupUser;
import handler.persistence.UserGroupUserMembership;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.UserGroupUserMembershipRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.BatchUserSaver;
import handler.utils.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService testUserService;

    @Mock
    private PagingAndSortingCrudRepository<UserEntity, String> mockUserRepository;

    @Mock
    private Filter<DescribeUsersRequestData, User> mockUserFilter;

    @Mock
    private UserGroupUserMembershipRepository mockUserGroupUserMembershipRepository;

    @Mock
    private UserGroupService mockUserGroupService;
    @Mock
    private BatchUserSaver mockBatchUserSaver;

    private final static String GROUP1_ID = "group1";
    private final static String GROUP2_ID = "group2";
    private final static String GROUP3_ID = "group3";
    private final static String adminString = "Admin";
    private final static String userString = "User";
    private final static List<String> testRoles = List.of(adminString, userString);

    private final static String testNormalImportFilePath = "src/test/resources/testImportFileNormal.csv";
    private final static String testBadImportFilePath = "src/test/resources/testImportFile.csv";

    private final static String USER1_ID = "user1";
    private final static String USER2_ID = "user2";
    private final static String USER3_ID = "user3";
    private final static String USER4_ID = "user4";
    private final static String USER5_ID = "user5";
    private final static String USER6_ID = "user6";
    private final static List<String> normalUsersList = List.of(USER1_ID, USER2_ID, USER3_ID, USER4_ID, USER5_ID, USER6_ID);
    private final static String testSortKey = "Name";

    private final static Map<String, List<String>> groupMembers = new HashMap<>() {{
        put(GROUP1_ID, List.of(USER1_ID, USER2_ID));
        put(GROUP2_ID, List.of(USER4_ID));
    }};

    @Test
    public void createUserSuccess() {
        when(mockUserRepository.existsById(any())).thenReturn(false);
        when(mockUserRepository.save(any())).thenReturn(new UserEntity());
        assertTrue(testUserService.createUser(GROUP1_ID, GROUP1_ID, GROUP1_ID));

        when(mockUserRepository.existsById(any())).thenReturn(true);
        assertFalse(testUserService.createUser(GROUP1_ID, GROUP1_ID, GROUP1_ID));
    }

    @Test
    public void describeUsersSuccess() {
        FilterToken filterToken = new FilterToken().operator(FilterToken.OperatorEnum.EQUAL).value(GROUP1_ID);
        DescribeUsersRequestData request = new DescribeUsersRequestData().addUserIdsItem(filterToken);
        request.setSortToken(new SortToken().operator(SortToken.OperatorEnum.ASC).key(testSortKey));
        request.setMaxResults(5);
        User user = new UserEntity().userId(GROUP1_ID);
        RepositoryResponse<UserEntity> mockRepositoryResponse = mock(RepositoryResponse.class);
        List<UserEntity> userEntities = List.of((UserEntity) user);
        doReturn(userEntities).when(mockRepositoryResponse).getItems();

        when(mockUserRepository.findAll(any(RepositoryRequest.class))).thenReturn(mockRepositoryResponse);
        when(mockUserFilter.getFiltered(any(), any())).thenReturn(userEntities.stream().map(u -> (User) u).toList());
        DescribeUsersResponse response = testUserService.describeUsers(request);
        assertEquals(1, response.getUsers().size());
        assertEquals(GROUP1_ID, response.getUsers().get(0).getUserId());
        assertNull(response.getUsers().get(0).getDisplayName());
        assertNull(response.getUsers().get(0).getRole());
        assertNull(response.getUsers().get(0).getIsDisabled());
        assertNull(response.getUsers().get(0).getDisabledReason());
        assertNull(response.getUsers().get(0).getIsImported());
        assertNull(response.getUsers().get(0).getCreationTime());
        assertNull(response.getUsers().get(0).getLastModifiedTime());
        assertNull(response.getUsers().get(0).getLastLoggedInTime());
        assertNull(response.getError());

        request.setUserIds(null);
        response = testUserService.describeUsers(request);
        assertEquals(1, response.getUsers().size());
        assertEquals(GROUP1_ID, response.getUsers().get(0).getUserId());
        assertNull(response.getError());

        filterToken.operator(FilterToken.OperatorEnum.NOT_EQUAL);
        request.addUserIdsItem(filterToken);
        response = testUserService.describeUsers(request);
        assertEquals(1, response.getUsers().size());
        assertEquals(GROUP1_ID, response.getUsers().get(0).getUserId());
        assertNull(response.getError());
    }

    @Test
    public void testDescribeUsersWithGroupFilterTokenJustEquals() {
        FilterTokenStrict includeFilterToken = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.EQUAL).value(GROUP1_ID);
        DescribeUsersRequestData request = new DescribeUsersRequestData()
                .addUserGroupIdsItem(includeFilterToken);
        request.setSortToken(new SortToken().operator(SortToken.OperatorEnum.ASC).key(testSortKey));
        request.setMaxResults(5);

        // Set up the repository to return a list of users
        RepositoryResponse<UserEntity> mockRepositoryResponse = mock(RepositoryResponse.class);
        List<UserEntity> userEntities = normalUsersList.stream().map(id -> (UserEntity) (new UserEntity().userId(id))).toList();
        doReturn(userEntities).when(mockRepositoryResponse).getItems();
        when(mockUserRepository.findAll(any(RepositoryRequest.class))).thenReturn(mockRepositoryResponse);

        doAnswer(invocation -> {
            // Don't do any filtering
            return invocation.getArgument(1);
        }).when(mockUserFilter).getFiltered(any(DescribeUsersRequestData.class), anyList());

        doAnswer(invocation -> {
            UserGroupUser id = invocation.getArgument(0);

            if (groupMembers.containsKey(id.getUserGroupId())) {
                return groupMembers.get(id.getUserGroupId()).contains(id.getUserId());
            }

            return false;
        }).when(mockUserGroupUserMembershipRepository).existsById(any());

        DescribeUsersResponse response = testUserService.describeUsers(request);
        assertNotNull(response);
        assertEquals(2, response.getUsers().size());
        assertEquals(USER1_ID, response.getUsers().get(0).getUserId());
        assertEquals(USER2_ID, response.getUsers().get(1).getUserId());
    }

    @Test
    public void testDescribeUsersWithGroupFilterTokenJustNotEquals() {
        FilterTokenStrict excludeFilterToken = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.NOT_EQUAL).value(GROUP2_ID);
        DescribeUsersRequestData request = new DescribeUsersRequestData()
                .addUserGroupIdsItem(excludeFilterToken);
        request.setSortToken(new SortToken().operator(SortToken.OperatorEnum.ASC).key(testSortKey));
        request.setMaxResults(5);

        // Set up the repository to return a list of users
        RepositoryResponse<UserEntity> mockRepositoryResponse = mock(RepositoryResponse.class);
        List<UserEntity> userEntities = normalUsersList.stream().map(id -> (UserEntity) (new UserEntity().userId(id))).toList();
        doReturn(userEntities).when(mockRepositoryResponse).getItems();
        when(mockUserRepository.findAll(any(RepositoryRequest.class))).thenReturn(mockRepositoryResponse);

        doAnswer(invocation -> {
            // Don't do any filtering
            return invocation.getArgument(1);
        }).when(mockUserFilter).getFiltered(any(DescribeUsersRequestData.class), anyList());

        doAnswer(invocation -> {
            UserGroupUser id = invocation.getArgument(0);

            if (groupMembers.containsKey(id.getUserGroupId())) {
                return groupMembers.get(id.getUserGroupId()).contains(id.getUserId());
            }

            return false;
        }).when(mockUserGroupUserMembershipRepository).existsById(any());

        DescribeUsersResponse response = testUserService.describeUsers(request);
        assertNotNull(response);
        assertEquals(5, response.getUsers().size());
        assertEquals(USER1_ID, response.getUsers().get(0).getUserId());
        assertEquals(USER2_ID, response.getUsers().get(1).getUserId());
        assertEquals(USER3_ID, response.getUsers().get(2).getUserId());
        assertEquals(USER5_ID, response.getUsers().get(3).getUserId());
        assertEquals(USER6_ID, response.getUsers().get(4).getUserId());
    }
    @Test
    public void testDescribeUsersWithGroupFilterTokenBoth() {
        FilterTokenStrict includeFilterToken = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.EQUAL).value(GROUP1_ID);
        FilterTokenStrict excludeFilterToken = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.NOT_EQUAL).value(GROUP2_ID);
        DescribeUsersRequestData request = new DescribeUsersRequestData()
                .addUserGroupIdsItem(includeFilterToken)
                .addUserGroupIdsItem(excludeFilterToken);
        request.setSortToken(new SortToken().operator(SortToken.OperatorEnum.ASC).key(testSortKey));
        request.setMaxResults(5);

        // Set up the repository to return a list of users
        RepositoryResponse<UserEntity> mockRepositoryResponse = mock(RepositoryResponse.class);
        List<UserEntity> userEntities = normalUsersList.stream().map(id -> (UserEntity) (new UserEntity().userId(id))).toList();
        doReturn(userEntities).when(mockRepositoryResponse).getItems();
        when(mockUserRepository.findAll(any(RepositoryRequest.class))).thenReturn(mockRepositoryResponse);

        doAnswer(invocation -> {
            // Don't do any filtering
            return invocation.getArgument(1);
        }).when(mockUserFilter).getFiltered(any(DescribeUsersRequestData.class), anyList());

        doAnswer(invocation -> {
            UserGroupUser id = invocation.getArgument(0);

            if (groupMembers.containsKey(id.getUserGroupId())) {
                return groupMembers.get(id.getUserGroupId()).contains(id.getUserId());
            }

            return false;
        }).when(mockUserGroupUserMembershipRepository).existsById(any());

        DescribeUsersResponse response = testUserService.describeUsers(request);
        assertNotNull(response);
        assertEquals(5, response.getUsers().size());
        assertEquals(USER1_ID, response.getUsers().get(0).getUserId());
        assertEquals(USER2_ID, response.getUsers().get(1).getUserId());
        assertEquals(USER3_ID, response.getUsers().get(2).getUserId());
        assertEquals(USER5_ID, response.getUsers().get(3).getUserId());
        assertEquals(USER6_ID, response.getUsers().get(4).getUserId());
    }

    @Test
    public void testDescribeUsersBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> testUserService.describeUsers(null));

        assertThrowsExactly(BadRequestException.class,
                () -> testUserService.describeUsers(new DescribeUsersRequestData().sortToken(new SortToken())));
    }

    @Test
    public void testImportUsersJustAdminNoExistingUsersNoOverwrite() throws Exception {
        MultipartFile file = new MockMultipartFile("File", new FileInputStream(testNormalImportFilePath));

        when(mockBatchUserSaver.getSuccessfulUsersList()).thenReturn(normalUsersList);
        when(mockBatchUserSaver.getUnsuccessfulUsersList()).thenReturn(Collections.emptyList());
        when(mockUserGroupService.createUserGroupOrReturnIfExists(any(), any(), Mockito.same(true))).thenReturn((UserGroupEntity) new UserGroupEntity().userGroupId("test"));

        // Capture each argument passed to saveUser to verify that it was called once for each element in the CSV
        final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        ImportUsersResponse response = testUserService.importUsers(file, false, false, testRoles, userString);

        verify(mockBatchUserSaver, times(6)).saveUser(captor.capture(), Mockito.same(false));
        verify(mockBatchUserSaver, times(1)).sendBatchAndClear();

        // Check that all arguments were correctly passed
        assertThat(captor.getAllValues().stream().map(UserEntity::getUserId)).containsExactlyInAnyOrderElementsOf(normalUsersList);

        assertEquals(response.getSuccessfulUsersList(), normalUsersList);
        assertEquals(0, response.getUnsuccessfulUsersList().size());
    }

    @Test
    public void testImportUsersBadUsersInFile() throws Exception {
        MultipartFile file = new MockMultipartFile("badFile", new FileInputStream(testBadImportFilePath));

        when(mockBatchUserSaver.getSuccessfulUsersList()).thenReturn(new ArrayList<>());
        when(mockBatchUserSaver.getUnsuccessfulUsersList()).thenReturn(new ArrayList<>());

        // Capture each argument passed to saveUser to verify that it was called once for each correct element in the CSV
        final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        ImportUsersResponse response = testUserService.importUsers(file, false, false, testRoles, userString);

        verify(mockBatchUserSaver, times(3)).saveUser(captor.capture(), Mockito.same(false));
        verify(mockBatchUserSaver, times(1)).sendBatchAndClear();

        assertEquals(2, response.getUnsuccessfulUsersList().size());

    }
    @Test
    public void testImportUsersBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> testUserService.importUsers(null, null, null, null, null));
    }

    @Test
    public void updateLastLoggedInTimeSuccess() {
        Optional<UserEntity> user = Optional.of((UserEntity) new UserEntity().userId(GROUP1_ID));
        when(mockUserRepository.save(user.get())).thenReturn(user.get());
        testUserService.updateLastLoggedInTime(GROUP1_ID);
        verify(mockUserRepository, times(0)).save(user.get());

        when(mockUserRepository.findById(GROUP1_ID)).thenReturn(user);
        testUserService.updateLastLoggedInTime(GROUP1_ID);
        verify(mockUserRepository, times(1)).save(user.get());
    }
}
