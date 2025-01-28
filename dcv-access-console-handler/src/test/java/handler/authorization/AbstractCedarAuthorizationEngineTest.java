// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization;

import handler.authorization.engines.CedarAuthorizationEngine;
import handler.authorization.engines.entities.SetShareListResponse;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.authorization.enums.SystemAction;
import handler.brokerclients.BrokerClient;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import handler.services.UserService;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.exception.AuthException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AuthorizationServiceException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public abstract class AbstractCedarAuthorizationEngineTest {

    private final BasicAuthorizationEngine mockBasicAuthorizationEngine;

    private final CedarAuthorizationEngine testRealCedarAuthorizationEngine;
    private final CedarAuthorizationEngine testMockedCedarAuthorizationEngine;

    // Using the main policy file for testing. Using testing specific role and user files.
    private final String policyLocation = "src/main/resources/authorization/policies.cedar";
    private final String shareLevelLocation = "src/main/resources/authorization/resource-sharing.json";
    private final String roleLocation = "src/test/resources/authorization/roles.json";

    File policyFile;
    File roleFile;

    private String ADMIN_UUID = "admin1";
    private String USER_UUID = "user1";
    private String USER2_UUID = "user2";
    private String USER3_UUID = "user3";
    private String USER4_UUID = "user4";
    private String GUEST_UUID = "guest1";
    private String FAKE_USER_UUID = "fakeUser";
    private String FAKE_GROUP_UUID = "fakeGroup";
    private final String GUEST_ROLE = "Guest";

    protected final boolean caseSensitive;

    private final List<String> USER_LIST = List.of(USER2_UUID, USER3_UUID, USER4_UUID);
    private final String USER_ROLE = "User";
    private final String GUEST_DISPLAY_NAME = "UserDisplayName";

    private final String SESSION_STRING = "session01";
    private final String SESSION_TEMPLATE_STRING = "sessionTemplate01";
    private final String DEVELOPER_GROUP_STRING = "developers";
    private final String PROJECT_GROUP_STRING = "project1";
    private final String PROJECT_2_GROUP_STRING = "project2";
    private final List<String> GROUP_LIST = List.of(PROJECT_2_GROUP_STRING);

    private final ObjectMapper mapper;
    private final ObjectMapper mockMapper;
    private final ObjectWriter mockWriter;

    private final UserService mockUserService;
    private final UserGroupService mockUserGroupService;
    private final SessionTemplateService mockSessionTemplateService;
    private final BrokerClient mockBrokerClient;

    @SuppressWarnings("unchecked")
    public AbstractCedarAuthorizationEngineTest(boolean caseSensitive) throws IOException {
        policyFile = new File(policyLocation);
        roleFile = new File(roleLocation);
        this.mapper = new ObjectMapper();
        this.mockMapper = mock(ObjectMapper.class);
        this.mockWriter = mock(ObjectWriter.class);
        this.mockUserService = mock(UserService.class);
        this.mockUserGroupService = mock(UserGroupService.class);
        this.mockSessionTemplateService = mock(SessionTemplateService.class);
        this.mockBrokerClient = mock(BrokerClient.class);

        // If it is not case-sensitive, change the UUID to upper case to verify
        this.caseSensitive = caseSensitive;
        if(!caseSensitive) {
            ADMIN_UUID = ADMIN_UUID.toUpperCase();
            USER_UUID =  USER_UUID .toUpperCase();
            USER2_UUID = USER2_UUID.toUpperCase();
            USER3_UUID = USER3_UUID.toUpperCase();
            USER4_UUID = USER4_UUID.toUpperCase();
            GUEST_UUID = GUEST_UUID.toUpperCase();
            FAKE_USER_UUID = FAKE_USER_UUID.toUpperCase();
            FAKE_GROUP_UUID = FAKE_GROUP_UUID.toUpperCase();
        }

        BasicAuthorizationEngine basicAuthorizationEngine = new BasicAuthorizationEngine();
        this.testRealCedarAuthorizationEngine = new CedarAuthorizationEngine(
                policyFile,
                roleFile,
                caseSensitive,
                basicAuthorizationEngine,
                mapper,
                mockWriter,
                mockUserService,
                mockUserGroupService,
                mockSessionTemplateService,
                mockBrokerClient
        );
        mockBasicAuthorizationEngine = mock(BasicAuthorizationEngine.class);

        when(mockMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(Collections.emptyList());
        this.testMockedCedarAuthorizationEngine = new CedarAuthorizationEngine(
                policyFile,
                roleFile,
                caseSensitive,
                mockBasicAuthorizationEngine,
                mockMapper,
                mockWriter,
                mockUserService,
                mockUserGroupService,
                mockSessionTemplateService,
                mockBrokerClient
        );
    }

    @Test
    public void testAdminCanDescribeHosts() {
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, ADMIN_UUID, SystemAction.describeServers));
    }

    @Test
    public void testUserCantDescribeHosts() {
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, SystemAction.describeServers));
    }

    @Test
    public void testUserCanDescribeSessions() {
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, SystemAction.describeSessions));
    }

    @Test
    public void testNonExistentUser() {
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, "randomUser", SystemAction.describeSessions));
    }

    @Test
    public void testGuestCantDescribeSessionTemplates() {
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, GUEST_UUID, SystemAction.describeSessionTemplates));
    }

    @Test
    public void testOwnerCanConnectToSession() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, USER_UUID));
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.connectToSession, ResourceType.Session, SESSION_STRING));
    }
    @Test
    public void testOwnerCantConnectToWrongSession() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, USER_UUID));
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.connectToSession, ResourceType.Session, "randomSession"));
    }

    @Test
    public void testAddSessionAndShare() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.Session, SESSION_STRING, ShareLevel.collaborators));

        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
    }

    @Test
    public void testAddSessionTemplateAndShare() {
        assertTrue(testRealCedarAuthorizationEngine.addSessionTemplate(SESSION_TEMPLATE_STRING, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.publishedTo));

        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING));
    }

    @Test
    public void testRepositoryError() throws IOException {
        doThrow(JsonProcessingException.class).when(mockMapper).writeValueAsString(any());
        assertFalse(testMockedCedarAuthorizationEngine.addSessionTemplate(SESSION_TEMPLATE_STRING, ADMIN_UUID));
    }

    @Test
    public void testOtherUserCantViewSession() {
        String sessionID = "session01";
        assertTrue(testRealCedarAuthorizationEngine.addSession(sessionID, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.Session, sessionID, ShareLevel.collaborators));

        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, GUEST_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, sessionID));
    }

    @Test
    public void testUserCanModifyThemselves() {
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.editUser, ResourceType.User, USER_UUID));
    }

    @Test
    public void testGuestCantModifyThemselves() {
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, GUEST_UUID, ResourceAction.editUser, ResourceType.User, GUEST_UUID));
    }

    @Test
    public void testUserCantModifyOthers() {
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.editUser, ResourceType.User, GUEST_UUID));
    }

    @Test
    public void testCantAddExistingUser() {
        assertFalse(testRealCedarAuthorizationEngine.addUserWithPersistence(USER_UUID));
    }


    @Test
    public void testAddNewUser() {
        assertTrue(testRealCedarAuthorizationEngine.addUserWithPersistence("NewUser"));
    }

    @Test
    public void testAddExistingRole() {
        assertFalse(testRealCedarAuthorizationEngine.addRole("Admin", Collections.emptyList()));
    }

    @Test
    public void testAddExistingSession() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertFalse(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
    }

    @Test
    public void testAddExistingSessionTemplate() {
        assertTrue(testRealCedarAuthorizationEngine.addSessionTemplate(SESSION_TEMPLATE_STRING, ADMIN_UUID));
        assertFalse(testRealCedarAuthorizationEngine.addSessionTemplate(SESSION_TEMPLATE_STRING, ADMIN_UUID));
    }

    @Test
    public void testAddSessionAndShareWithWrongList() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertFalse(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.Session, SESSION_STRING, ShareLevel.publishedTo));
    }

    @Test
    public void testAddSessionAlreadySharedUser() {
        assertTrue(testRealCedarAuthorizationEngine.addSessionTemplate(SESSION_TEMPLATE_STRING, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.publishedTo));
        assertFalse(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.publishedTo));
    }

    @Test
    public void testAddSessionAndShareWithWrongResource() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertFalse(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.Session, "fakeSession", ShareLevel.collaborators));
    }

    @Test
    public void testAddSessionAndShareWithWrongPrincipal() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertFalse(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, FAKE_USER_UUID, ResourceType.Session, SESSION_STRING, ShareLevel.collaborators));
    }

    @Test
    public void testSetShareList() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.Session, SESSION_STRING, ShareLevel.collaborators));
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));

        SetShareListResponse response = testRealCedarAuthorizationEngine.setShareList(USER_LIST, GROUP_LIST, ResourceType.Session, SESSION_STRING, ShareLevel.collaborators);
        assertEquals(new HashSet<>(GROUP_LIST), new HashSet<>(response.getSuccessfulGroups()));
        assertEquals(new HashSet<>(USER_LIST), new HashSet<>(response.getSuccessfulUsers()));
        assertTrue(response.getUnSuccessfulUsers().isEmpty());
        assertTrue(response.getUnSuccessfulGroups().isEmpty());

        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER2_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER3_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER4_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.Group, PROJECT_2_GROUP_STRING, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));

    }

    @Test
    public void testDedupShareList() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));

        List<String> dupedUserList =  List.of(USER_UUID, USER_UUID);
        SetShareListResponse response = testRealCedarAuthorizationEngine.setShareList(dupedUserList,
                Collections.emptyList(), ResourceType.Session, SESSION_STRING, ShareLevel.collaborators);
        assertEquals(1, response.getSuccessfulUsers().size());
        assertEquals(1, testRealCedarAuthorizationEngine.getSharedListForResource(ResourceType.Session, SESSION_STRING, ShareLevel.collaborators, PrincipalType.User).size());
    }

    @Test
    public void testAddSessionAndSetShareListWrongResource() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        SetShareListResponse response = testRealCedarAuthorizationEngine.setShareList(USER_LIST,
                Collections.emptyList(), ResourceType.Session, "fakeSession", ShareLevel.collaborators);
        assertEquals(USER_LIST, response.getUnSuccessfulUsers());
    }

    @Test
    public void testAddSessionAndSetWrongShareLevel() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        SetShareListResponse response = testRealCedarAuthorizationEngine.setShareList(USER_LIST,
                Collections.emptyList(), ResourceType.Session, SESSION_STRING, ShareLevel.sharedWith);
        assertEquals(USER_LIST, response.getUnSuccessfulUsers());
    }

    @Test
    public void testAddSessionSetShareListWithWrongUser() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        SetShareListResponse response = testRealCedarAuthorizationEngine.setShareList(List.of(FAKE_USER_UUID),
                Collections.emptyList(), ResourceType.Session, SESSION_STRING, ShareLevel.collaborators);
        assertEquals(List.of("fakeUser"), response.getUnSuccessfulUsers());
    }

    @Test
    public void testAddSessionSetShareListWithWrongGroup() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        SetShareListResponse response = testRealCedarAuthorizationEngine.setShareList(Collections.emptyList(),
                List.of("fakeGroup"), ResourceType.Session, SESSION_STRING, ShareLevel.collaborators);
        assertEquals(List.of("fakeGroup"), response.getUnSuccessfulGroups());

    }

    @Test
    public void testDeleteFakeResource() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertFalse(testRealCedarAuthorizationEngine.deleteResource(ResourceType.Session, "fakeSession"));
    }

    @Test
    public void testConnectToSessionWithSharedGroup() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, USER_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.Group, DEVELOPER_GROUP_STRING, ResourceType.Session, SESSION_STRING, ShareLevel.collaborators));
        assertTrue(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, GUEST_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
    }

    @Test
    public void testCantConnectToSessionWithNoSharedGroup() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, USER_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.Group, PROJECT_GROUP_STRING, ResourceType.Session, SESSION_STRING, ShareLevel.collaborators));
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, GUEST_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
    }

    @Test
    public void testDeleteResource() {
        assertTrue(testRealCedarAuthorizationEngine.addSession(SESSION_STRING, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.deleteResource(ResourceType.Session, SESSION_STRING));
        assertFalse(testRealCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, SESSION_STRING));
    }

    @Test
    public void testLoadBadPolicyFile() {
        File badPolicyFile = new File("fakeLocation");
        assertThrows(RuntimeException.class, () -> {
            new CedarAuthorizationEngine(
                    badPolicyFile,
                    roleFile,
                    caseSensitive,
                    new BasicAuthorizationEngine(),
                    mapper,
                    mockWriter,
                    mockUserService,
                    mockUserGroupService,
                    mockSessionTemplateService,
                    mockBrokerClient
            );
        });
    }

    @Test
    public void testLoadBadRoleFile() {
        File badRoleFile = new File("fakeLocation");
        assertThrows(RuntimeException.class, () -> {
            new CedarAuthorizationEngine(
                    policyFile,
                    badRoleFile,
                    caseSensitive,
                    new BasicAuthorizationEngine(),
                    mapper,
                    mockWriter,
                    mockUserService,
                    mockUserGroupService,
                    mockSessionTemplateService,
                    mockBrokerClient
            );
        });
    }

    @Test
    public void testAuthorizationException() throws AuthException {
        when(mockBasicAuthorizationEngine.isAuthorized(any(), any(), any())).thenThrow(new AuthException("DummyException"));
        assertThrows(AuthorizationServiceException.class, () -> {
            testMockedCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, SystemAction.describeServers);
        });
    }

    @Test
    public void testAuthorizationExceptionOnResource() throws AuthException{
        when(mockBasicAuthorizationEngine.isAuthorized(any(), any(), any())).thenThrow(new AuthException("DummyException"));
        assertThrows(AuthorizationServiceException.class, () -> {
            testMockedCedarAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.viewSessionDetails, ResourceType.Session, "session");
        });
    }

    @Test
    public void testGetSharedToPrincipalList() {
        assertTrue(testRealCedarAuthorizationEngine.addSessionTemplate(SESSION_TEMPLATE_STRING, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.publishedTo));
        List<String> sharedToPrincipalList = testRealCedarAuthorizationEngine.getSharedListForResource(
                ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.publishedTo, PrincipalType.User);
        assertEquals(sharedToPrincipalList.size(), 1);
        assertEquals(sharedToPrincipalList.get(0), USER_UUID);
    }

    @Test
    public void testResourceNotFoundGetSharedToPrincipalList() {
        List<String> sharedToPrincipalList = testRealCedarAuthorizationEngine.getSharedListForResource(
                ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.publishedTo, PrincipalType.User);
        assertEquals(sharedToPrincipalList.size(), 0);
    }

    @Test
    public void testResourceInvalidShareLevelGetSharedToPrincipalList() {
        assertTrue(testRealCedarAuthorizationEngine.addSessionTemplate(SESSION_TEMPLATE_STRING, ADMIN_UUID));
        assertTrue(testRealCedarAuthorizationEngine.addPrincipalToSharedList(PrincipalType.User, USER_UUID, ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.publishedTo));
        List<String> sharedToPrincipalList = testRealCedarAuthorizationEngine.getSharedListForResource(
                ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING, ShareLevel.sharedWith, PrincipalType.User);
        assertEquals(sharedToPrincipalList.size(), 0);
    }

    @Test
    public void testLoadEntitiesFromRepository() {
        CedarAuthorizationEngine testAuthorizationEngine = new CedarAuthorizationEngine(
                policyFile,
                roleFile, caseSensitive,
                new BasicAuthorizationEngine(),
                mapper,
                mockWriter,
                mockUserService,
                mockUserGroupService,
                mockSessionTemplateService,
                mockBrokerClient
        );
        assertTrue(testAuthorizationEngine.isAuthorized(PrincipalType.User, USER_UUID, ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, SESSION_TEMPLATE_STRING));
    }
}
