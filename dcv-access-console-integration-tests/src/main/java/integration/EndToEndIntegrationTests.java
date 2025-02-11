// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package integration;

import integration.util.AuthServerUtils;
import integration.util.CustomTrustManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Reservation;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;


@Slf4j
public class EndToEndIntegrationTests {
    private static final int NUMBER_OF_RETRIES = 30;
    private static final int TIME_BETWEEN_RETRIES_MS = 10000;

    private static final String ADMIN1_USERNAME = "admin1";
    private static final String USER1_USERNAME = "user1";
    private static final String USER2_USERNAME = "user2";
    private static final String GUEST1_USERNAME = "guest1";
    private static final String GROUP1_NAME = "group1";

    private static final String SESSION_TEMPLATES_KEY = "SessionTemplates";
    private static final String SESSION_TEMPLATE_KEY = "SessionTemplate";
    private static final String SESSION_KEY = "Session";
    private static final String SESSION_ID_KEY = "SessionId";
    private static final String SESSIONS_KEY = "Sessions";
    private static final String USERS_KEY = "Users";
    private static final String USER_ID_KEY = "UserId";
    private static final String USER_GROUPS_KEY = "UserGroups";
    private static final String USER_GROUP_KEY = "UserGroup";
    private static final String USER_GROUP_ID_KEY = "UserGroupId";
    private static final String ERROR_KEY = "Error";
    private static final String ID_KEY = "Id";
    private static final String NAME_KEY = "Name";
    private static final String STATE_KEY = "State";
    private static final String READY_STATE = "READY";
    private static final String CREATED_BY_KEY = "CreatedBy";
    private static final String OWNER_KEY = "Owner";
    private static final String SUCCESSFUL_LIST_KEY = "SuccessfulList";
    private static final String UNSUCCESSFUL_LIST_KEY = "UnsuccessfulList";
    private static final String FAILURE_REASONS_KEY = "FailureReasons";
    private static final String CONNECTION_TOKEN_KEY = "ConnectionToken";
    private static final String SUCCESSFUL_USERS_LIST_KEY = "SuccessfulUsersList";
    private static final String UNSUCCESSFUL_USERS_LIST_KEY = "UnsuccessfulUsersList";
    private static final String SUCCESSFUL_GROUPS_LIST_KEY = "SuccessfulGroupsList";
    private static final String UNSUCCESSFUL_GROUPS_LIST_KEY = "UnsuccessfulGroupsList";
    private static final String SUCCESSFUL_SESSION_TEMPLATE_LIST_KEY = "SuccessfulSessionTemplateList";
    private static final String UNSUCCESSFUL_SESSION_TEMPLATE_LIST_KEY = "UnsuccessfulSessionTemplateList";
    private static final String UNAUTHORIZED_KEY = "Unauthorized";
    private static final String SERVERS_KEY = "Servers";

    private static final String SESSION_TEMPLATE_NAME = "testTemplate";
    private static final String SESSION_NAME = "testSession";
    private static final String DESCRIBE_SESSIONS_JSON = "{\"States\":[{\"Operator\":\"!=\",\"Value\":\"DELETED\"}]}";
    private static final String EMPTY_JSON = "{}";
    private static final String CREATE_SESSION_TEMPLATE_JSON = "{\"Name\":\"" + SESSION_TEMPLATE_NAME + "\",\"OsFamily\":\"linux\",\"Type\":\"CONSOLE\"}";
    private static final String EDIT_SESSION_TEMPLATE_JSON_FORMAT_STRING = "{\"TemplateId\":\"%s\", \"CreateSessionTemplateRequestData\":" + CREATE_SESSION_TEMPLATE_JSON + "}";
    private static final String VALIDATE_SESSION_TEMPLATE_JSON = "{\"CreateSessionTemplateRequestData\":" + CREATE_SESSION_TEMPLATE_JSON + ",\"IgnoreExisting\": false}";
    private static final String CREATE_SESSION_JSON_FORMAT_STRING = "[{\"Name\":\"" + SESSION_NAME + "\",\"SessionTemplateId\":\"%s\"}]";
    private static final String DELETE_SESSION_TEMPLATE_JSON_FORMAT_STRING = "{\"Ids\":[\"%s\"]}";
    private static final String DELETE_SESSION_JSON_FORMAT_STRING = "[{\"SessionId\":\"%s\",\"Owner\":\"%s\"}]";
    private static final String PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING = "{\"Id\":\"%s\",\"UserIds\":[%s],\"GroupIds\":[%s]}";
    private static final String DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING = "{\"SessionTemplateId\":\"%s\"}";
    private static final String CREATE_USER_GROUP_JSON_FORMAT_STRING = "{\"UserGroupId\":\"%s\"}";
    private static final String EDIT_USER_GROUP_JSON_FORMAT_STRING = "{\"UserGroupId\":\"%s\",\"UserIdsToAdd\":[%s]}";
    private static final String DELETE_USER_GROUPS_JSON_FORMAT_STRING = "{\"UserGroupIds\":[\"%s\"],\"DeleteIfNotEmpty\":true}";

    private static final String LIST_WITH_QUOTES_DELIMITER = "\",\"";
    private static final String ESCAPED_QUOTE = "\"";
    private static final String EMPTY_STRING = "";
    private static final String IMPORT_FILE_CONTENT = "\"userId\",\"displayName\",\"role\",\"groups\"\n" +
            "\"user2\",,\"User\",";

    private static AuthServerUtils authServerUtils;

    private static String sessionTemplateId01;
    private static String sessionId01;
    private static String sessionId02;
    private static String sessionId03;

    private static CloseableHttpClient httpClient;

    private String importUsersEndpoint;
    private String describeUsersEndpoint;
    private String createUserGroupEndpoint;
    private String editUserGroupEndpoint;
    private String describeUserGroupsEndpoint;
    private String deleteUserGroupsEndpoint;
    private String describeSessionTemplatesEndpoint;
    private String describeSessionEndpoint;
    private String describeUsersSharedWithSessionEndpoint;
    private String describeUsersSharedWithSessionTemplateEndpoint;
    private String describeUserGroupsSharedWithSessionTemplateEndpoint;
    private String createSessionTemplateEndpoint;
    private String editSessionTemplateEndpoint;
    private String createSessionEndpoint;
    private String deleteSessionTemplateEndpoint;
    private String deleteSessionEndpoint;
    private String validateSessionTemplateEndpoint;
    private String describeServersEndpoint;
    private String connectSessionEndpointFormatString;
    private String publishSessionTemplateEndpoint;
    private String unpublishSessionTemplateEndpoint;

    private static Ec2Client createEC2Client() {
        String regionName = System.getenv("AWS_REGION");
        Region region = Region.of(regionName);
        return Ec2Client.builder()
                .region(region)
                .build();
    }

    private static String getInstanceDns(String instanceName, String securityGroupName) {
        try (Ec2Client ec2 = createEC2Client()) {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .filters(
                            Filter.builder().
                                    name("tag:Name")
                                    .values(instanceName)
                                    .build(),
                            Filter.builder()
                                    .name("instance.group-name")
                                    .values(securityGroupName + '*')  // prefix match
                                    .build())
                    .build();
            DescribeInstancesResponse response = ec2.describeInstances(request);
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    InstanceStateName state = instance.state().name();
                    if (InstanceStateName.RUNNING == state) {
                        // We only need one instance IP from each ASG, so we return on the first one we find.
                        log.info(instance.publicDnsName());
                        return instance.publicDnsName();
                    }
                }
            }
            return null;
        }
    }

    @BeforeTest
    @Parameters({"instanceName", "ssmParameterSuffix", "securityGroupNamePrefix"})
    public void setup(String instanceName, String ssmParameterSuffix, String securityGroupNamePrefix) {
        log.info("Setting up integration tests for {}", instanceName);
        String instanceDns = getInstanceDns(instanceName, securityGroupNamePrefix);
        assertNotNull(instanceDns, "Unable to retrieve instance IP of instance with name: " + instanceName);
        instanceDns = "https://" + instanceDns;

        this.describeSessionTemplatesEndpoint = instanceDns + "/accessconsolehandler/describeSessionTemplates";
        this.describeSessionEndpoint = instanceDns + "/accessconsolehandler/describeSessions";
        this.createSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/createSessionTemplate";
        this.createSessionEndpoint = instanceDns + "/accessconsolehandler/createSessions";
        this.deleteSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/deleteSessionTemplates";
        this.deleteSessionEndpoint = instanceDns + "/accessconsolehandler/deleteSessions";
        this.validateSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/validateSessionTemplate";
        this.describeServersEndpoint = instanceDns + "/accessconsolehandler/describeServers";
        this.connectSessionEndpointFormatString = instanceDns + "/accessconsolehandler/getSessionConnectionData/%s";
        this.publishSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/publishSessionTemplate";
        this.importUsersEndpoint = instanceDns + "/accessconsolehandler/importUsers";
        this.describeUsersEndpoint = instanceDns + "/accessconsolehandler/describeUsers";
        this.createUserGroupEndpoint = instanceDns + "/accessconsolehandler/createUserGroup";
        this.editUserGroupEndpoint = instanceDns + "/accessconsolehandler/editUserGroup";
        this.describeUserGroupsEndpoint = instanceDns + "/accessconsolehandler/describeUserGroups";
        this.deleteUserGroupsEndpoint = instanceDns + "/accessconsolehandler/deleteUserGroups";
        this.describeUsersSharedWithSessionEndpoint = instanceDns + "/accessconsolehandler/describeUsersSharedWithSession";
        this.describeUsersSharedWithSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/describeUsersSharedWithSessionTemplate";
        this.describeUserGroupsSharedWithSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/describeUserGroupsSharedWithSessionTemplate";
        this.unpublishSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/unpublishSessionTemplate";
        this.editSessionTemplateEndpoint = instanceDns + "/accessconsolehandler/editSessionTemplate";

        authServerUtils = new AuthServerUtils(instanceDns, ssmParameterSuffix);

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            CustomTrustManager trustManager = new CustomTrustManager();
            sslContext.init(null, new CustomTrustManager[]{trustManager}, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Unable to setup custom Trust Manager.");
            throw new RuntimeException(e);
        }

        httpClient = HttpClients.custom().setSSLContext(sslContext).build();
        cleanUpSessions();
    }

    public void cleanUpSessions() {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(ADMIN1_USERNAME);
        try {
            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, describeSessionEndpoint, EMPTY_JSON, true);
            log.info("Called {}  with body {} and got response: {}", describeSessionEndpoint, EMPTY_JSON, response);

            assertNotNull(response);
            assertTrue(response.containsKey(SESSIONS_KEY));
            assertNotNull(response.get(SESSIONS_KEY));
            assertEquals(response.get(SESSIONS_KEY).getClass(), ArrayList.class);
            ArrayList<Object> describeResults = ((ArrayList<Object>)response.get(SESSIONS_KEY));
            log.info("Describe Results: {}", describeResults);

            if(describeResults.size() > 0 ) {
                log.warn("There are {} existing sessions, cleaning them up", describeResults.size());
                describeResults.stream().anyMatch(resource -> {
                    if (resource instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> resourceMap = (LinkedHashMap<String, Object>) resource;
                        String sessionId = resourceMap.get(ID_KEY).toString();
                        String state = resourceMap.get(STATE_KEY).toString();
                        if(!"DELETED".equals(state)) {
                            log.warn("Deleting session with id: {} with status: {}", sessionId, state);
                            String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId, ADMIN1_USERNAME);
                            canDeleteResource(ADMIN1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
                        }
                    }
                    return false;
                });
            }
        } catch (IOException e) {
            log.warn("Error while cleaning up the sessions, might not be an issue", e);
        }
    }

    @Test
    public void testAdminCanImportUser2() {
        testImportUsers(ADMIN1_USERNAME, List.of(USER2_USERNAME));
    }

    @Test
    public void testUser1CantImportUser2() {
        cantImportUsers(USER1_USERNAME);
    }

    @Test
    public void testUser2CantImportUser2() {
        cantImportUsers(USER2_USERNAME);
    }

    @Test
    public void testGuest1CantImportUser2() {
        cantImportUsers(GUEST1_USERNAME);
    }

    @Test(groups = {"zeroState"})
    public void testAdminCanDescribeSessionTemplates() {
        testUserDescribe(ADMIN1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test(groups = {"zeroState"})
    public void testUser1CanDescribeSessionTemplates() {
        testUserDescribe(USER1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test(groups = {"zeroState"})
    public void testUser2CanDescribeSessionTemplates() {
        testUserDescribe(USER2_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test(groups = {"zeroState"})
    public void testGuest1CantDescribeSessionTemplates() {
        userCantTakeSystemAction(GUEST1_USERNAME, describeSessionTemplatesEndpoint, EMPTY_JSON);
    }

    @Test(groups = {"zeroState"})
    public void testAdminCanDescribeSessions() {
        testUserDescribe(ADMIN1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(groups = {"zeroState"})
    public void testUser1CanDescribeSessions() {
        testUserDescribe(USER1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(groups = {"zeroState"})
    public void testUser2CanDescribeSessions() {
        testUserDescribe(USER2_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(groups = {"zeroState"})
    public void testGuest1CanDescribeSessions() {
        testUserDescribe(GUEST1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(groups = {"zeroState"})
    public void testUser1CantDescribeUsers() {
        userCantTakeSystemAction(USER1_USERNAME, describeUsersEndpoint, EMPTY_JSON);
    }

    @Test(groups = {"zeroState"})
    public void testUser2CantDescribeUsers() {
        userCantTakeSystemAction(USER2_USERNAME, describeUsersEndpoint, EMPTY_JSON);
    }

    @Test(groups = {"zeroState"})
    public void testGuest1CantDescribeUsers() {
        userCantTakeSystemAction(GUEST1_USERNAME, describeUsersEndpoint, EMPTY_JSON);
    }

    @Test(groups = {"zeroState"})
    public void testAdminCanDescribeUsers() {
        testUserDescribe(ADMIN1_USERNAME, describeUsersEndpoint, USERS_KEY, EMPTY_JSON, List.of(ADMIN1_USERNAME, USER1_USERNAME, USER2_USERNAME, GUEST1_USERNAME));
    }

    @Test(groups = {"zeroState"})
    public void testUser1CantDescribeUserGroups() {
        userCantTakeSystemAction(USER1_USERNAME, describeUserGroupsEndpoint, EMPTY_JSON);
    }

    @Test(groups = {"zeroState"})
    public void testUser2CantDescribeUserGroups() {
        userCantTakeSystemAction(USER2_USERNAME, describeUserGroupsEndpoint, EMPTY_JSON);
    }

    @Test(groups = {"zeroState"})
    public void testGuest1CantDescribeUserGroups() {
        userCantTakeSystemAction(GUEST1_USERNAME, describeUserGroupsEndpoint, EMPTY_JSON);
    }

    @Test(groups = {"zeroState"})
    public void testAdminCanDescribeUserGroups() {
        testUserDescribe(ADMIN1_USERNAME, describeUserGroupsEndpoint, USER_GROUPS_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test
    public void testAdminCanDescribeHosts() {
        testUserDescribe(ADMIN1_USERNAME, describeServersEndpoint, SERVERS_KEY, EMPTY_JSON);
    }

    @Test
    public void testUser1CantDescribeHosts() {
        userCantTakeSystemAction(USER1_USERNAME, describeServersEndpoint, EMPTY_JSON);
    }

    @Test
    public void testUser2CantDescribeHosts() {
        userCantTakeSystemAction(USER2_USERNAME, describeServersEndpoint, EMPTY_JSON);
    }

    @Test
    public void testGuest1CantDescribeHosts() {
        userCantTakeSystemAction(GUEST1_USERNAME, describeServersEndpoint, EMPTY_JSON);
    }

    @Test
    public void testAdminCanValidateSessionTemplate() {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(ADMIN1_USERNAME);
        try {
            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, validateSessionTemplateEndpoint, VALIDATE_SESSION_TEMPLATE_JSON, true);
            log.info("Called connectSession with body {} and got response: {}", VALIDATE_SESSION_TEMPLATE_JSON, response);
            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(FAILURE_REASONS_KEY));
            assertNotNull(response.get(FAILURE_REASONS_KEY));
            assertEquals(response.get(FAILURE_REASONS_KEY).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> failureReasons = (LinkedHashMap<String, Object>)response.get(FAILURE_REASONS_KEY);
            assertEquals(failureReasons.size(), 0);
            log.info("Successfully validatedSessionTemplate as user {} ", ADMIN1_USERNAME);
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + validateSessionTemplateEndpoint + " failed.", e);
        }
    }

    @Test
    public void testUser1CantValidateSessionTemplate() {
        userCantTakeSystemAction(USER1_USERNAME, validateSessionTemplateEndpoint, VALIDATE_SESSION_TEMPLATE_JSON);
    }

    @Test
    public void testUser2CantValidateSessionTemplate() {
        userCantTakeSystemAction(USER2_USERNAME, validateSessionTemplateEndpoint, VALIDATE_SESSION_TEMPLATE_JSON);
    }

    @Test
    public void testGuest1CantValidateSessionTemplate() {
        userCantTakeSystemAction(GUEST1_USERNAME, validateSessionTemplateEndpoint, VALIDATE_SESSION_TEMPLATE_JSON);
    }

    @Test(dependsOnGroups = {"zeroState"})
    public void testAdminCanCreateUserGroup() {
        testCreateUserGroup(ADMIN1_USERNAME, GROUP1_NAME);
    }

    @Test
    public void testUser1CantCreateUserGroup() {
        String createUserGroupBody = String.format(CREATE_USER_GROUP_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(USER1_USERNAME, createUserGroupEndpoint, createUserGroupBody);
    }

    @Test
    public void testUser2CantCreateUserGroup() {
        String createUserGroupBody = String.format(CREATE_USER_GROUP_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(USER2_USERNAME, createUserGroupEndpoint, createUserGroupBody);
    }

    @Test
    public void testGuest1CantCreateUserGroup() {
        String createUserGroupBody = String.format(CREATE_USER_GROUP_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(GUEST1_USERNAME, createUserGroupEndpoint, createUserGroupBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateUserGroup"})
    public void testAdminCanAddUser2ToGroup1() {
        testEditUserGroup(ADMIN1_USERNAME, GROUP1_NAME, List.of(USER2_USERNAME));
    }

    @Test(dependsOnMethods = {"testAdminCanCreateUserGroup"})
    public void testUser1CantAddUser2ToGroup1() {
        cantEditUserGroup(USER1_USERNAME, GROUP1_NAME, List.of(USER2_USERNAME));
    }

    @Test(dependsOnMethods = {"testAdminCanCreateUserGroup"})
    public void testUser2CantAddUser2ToGroup1() {
        cantEditUserGroup(USER2_USERNAME, GROUP1_NAME, List.of(USER2_USERNAME));
    }

    @Test(dependsOnMethods = {"testAdminCanCreateUserGroup"})
    public void testGuest1CantAddUser2ToGroup1() {
        cantEditUserGroup(GUEST1_USERNAME, GROUP1_NAME, List.of(USER2_USERNAME));
    }

    @Test(dependsOnMethods = {"testAdminCanCreateUserGroup"})
    public void testUser1CantDeleteGroup() {
        String deleteUsersGroupBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(USER1_USERNAME, deleteUserGroupsEndpoint, deleteUsersGroupBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateUserGroup"})
    public void testUser2CantDeleteGroup() {
        String deleteUsersGroupBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(USER2_USERNAME, deleteUserGroupsEndpoint, deleteUsersGroupBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateUserGroup"})
    public void testGuest1CantDeleteGroup() {
        String deleteUsersGroupBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(GUEST1_USERNAME, deleteUserGroupsEndpoint, deleteUsersGroupBody);
    }

    @Test(dependsOnGroups = {"zeroState"})
    public void testAdminCanCreateSessionTemplate01() {
        sessionTemplateId01 = userCanCreateSessionTemplate(ADMIN1_USERNAME);
    }

    @Test
    public void testUser1CantCreateSessionTemplate() {
        userCantTakeSystemAction(USER1_USERNAME, createSessionTemplateEndpoint, CREATE_SESSION_TEMPLATE_JSON);
    }

    @Test
    public void testUser2CantCreateSessionTemplate() {
        userCantTakeSystemAction(USER2_USERNAME, createSessionTemplateEndpoint, CREATE_SESSION_TEMPLATE_JSON);
    }

    @Test
    public void testGuest1CantCreateSessionTemplate() {
        userCantTakeSystemAction(GUEST1_USERNAME, createSessionTemplateEndpoint, CREATE_SESSION_TEMPLATE_JSON);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testAdminCanEditSessionTemplate01() {
        String editSessionTemplateBody = String.format(EDIT_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        sessionTemplateId01 = userCanEditSessionTemplate(ADMIN1_USERNAME, editSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser1CantEditSessionTemplate() {
        String editSessionTemplateBody = String.format(EDIT_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER1_USERNAME, editSessionTemplateEndpoint, editSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser2CantEditSessionTemplate() {
        String editSessionTemplateBody = String.format(EDIT_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER2_USERNAME, editSessionTemplateEndpoint, editSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testGuest1CantEditSessionTemplate() {
        String editSessionTemplateBody = String.format(EDIT_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(GUEST1_USERNAME, editSessionTemplateEndpoint, editSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testAdminCanDescribeUsersSharedWithSessionTemplate01() {
        String describeUsersSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        testUserDescribe(ADMIN1_USERNAME, describeUsersSharedWithSessionTemplateEndpoint, USERS_KEY, describeUsersSharedWithSessionTemplateBody, Collections.emptyList());
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser1CantDescribeUsersSharedWithSessionTemplate() {
        String describeUsersSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER1_USERNAME, describeUsersSharedWithSessionTemplateEndpoint, describeUsersSharedWithSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser2CantDescribeUsersSharedWithSessionTemplate() {
        String describeUsersSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER2_USERNAME, describeUsersSharedWithSessionTemplateEndpoint, describeUsersSharedWithSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testGuest1CantDescribeUsersSharedWithSessionTemplate() {
        String describeUsersSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(GUEST1_USERNAME, describeUsersSharedWithSessionTemplateEndpoint, describeUsersSharedWithSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testAdminCanDescribeUserGroupsSharedWithSessionTemplate01() {
        String describeUserGroupsSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        testUserDescribe(ADMIN1_USERNAME, describeUserGroupsSharedWithSessionTemplateEndpoint, USER_GROUPS_KEY, describeUserGroupsSharedWithSessionTemplateBody, Collections.emptyList());
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser1CantDescribeUserGroupsSharedWithSessionTemplate() {
        String describeUserGroupsSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER1_USERNAME, describeUserGroupsSharedWithSessionTemplateEndpoint, describeUserGroupsSharedWithSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser2CantDescribeUserGroupsSharedWithSessionTemplate() {
        String describeUserGroupsSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER2_USERNAME, describeUserGroupsSharedWithSessionTemplateEndpoint, describeUserGroupsSharedWithSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testGuest1CantDescribeUserGroupsSharedWithSessionTemplate() {
        String describeUserGroupsSharedWithSessionTemplateBody = String.format(DESCRIBE_USERS_GROUPS_SHARED_WITH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(GUEST1_USERNAME, describeUserGroupsSharedWithSessionTemplateEndpoint, describeUserGroupsSharedWithSessionTemplateBody);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testAdmin1CanSeeSessionTemplate01() {
        testUserDescribe(ADMIN1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.singletonList(sessionTemplateId01));
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser1CantSeeSessionTemplate01() {
        testUserDescribe(USER1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testUser2CantSeeSessionTemplate01() {
        testUserDescribe(USER2_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"})
    public void testGuest1CantSeeSessionTemplate01() {
        userCantTakeSystemAction(GUEST1_USERNAME, describeSessionTemplatesEndpoint, EMPTY_JSON);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"}, dependsOnGroups = { "zeroState" }, groups = { "firstSessionCreation" })
    public void testAdminCanCreateSession() {
        assertNotNull(sessionTemplateId01);
        sessionId01 = userCanCreateSession(ADMIN1_USERNAME, sessionTemplateId01);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"}, groups = { "firstSessionCreation" } )
    public void testUser1CantCreateSession() {
        userCantCreateSession(USER1_USERNAME, sessionTemplateId01);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"}, groups = { "firstSessionCreation" } )
    public void testUser2CantCreateSession() {
        userCantCreateSession(USER2_USERNAME, sessionTemplateId01);
    }

    @Test(dependsOnMethods = {"testAdminCanCreateSessionTemplate01"}, groups = { "firstSessionCreation" } )
    public void testGuest1CantCreateSession() {
        String createSessionBody = String.format(CREATE_SESSION_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(GUEST1_USERNAME, createSessionEndpoint, createSessionBody);
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testAdminCanViewSession01() {
        testUserDescribe(ADMIN1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, List.of(sessionId01));
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testUser1CantViewSession01() {
        testUserDescribe(USER1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testUser2CantViewSession01() {
        testUserDescribe(USER2_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testGuest1CantViewSession01() {
        testUserDescribe(GUEST1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testAdminCanConnectToSession01() {
        waitUntilSessionIsReady(ADMIN1_USERNAME, sessionId01);
        userCanConnectToSession(ADMIN1_USERNAME, sessionId01);
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testUser1CantConnectToSession01() {
        userCantConnectToSession(USER1_USERNAME, sessionId01);
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testUser2CantConnectToSession01() {
        userCantConnectToSession(USER2_USERNAME, sessionId01);
    }

    @Test(dependsOnMethods = { "testAdminCanCreateSession" })
    public void testGuest1CantConnectToSession01() {
        userCantConnectToSession(GUEST1_USERNAME, sessionId01);
    }

    @Test(dependsOnGroups = { "firstSessionCreation" })
    public void testAdminCanPublishSessionTemplate01ToUser1() {
        userCanPublishSessionTemplate(ADMIN1_USERNAME, sessionTemplateId01, List.of(USER1_USERNAME), Collections.emptyList());
    }

    @Test(dependsOnGroups = { "firstSessionCreation" })
    public void testUser1CantPublishSessionTemplate01ToUser1() {
        String publishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionId01, USER1_USERNAME, "");
        userCantTakeSystemAction(USER1_USERNAME, publishSessionTemplateEndpoint, publishSessionTemplateBody);
    }

    @Test(dependsOnGroups = { "firstSessionCreation" })
    public void testUser2CantPublishSessionTemplate01ToUser1() {
        String publishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionId01, USER2_USERNAME, "");
        userCantTakeSystemAction(USER2_USERNAME, publishSessionTemplateEndpoint, publishSessionTemplateBody);
    }

    @Test(dependsOnGroups = { "firstSessionCreation" })
    public void testGuest1CantPublishSessionTemplate01ToUser1() {
        String publishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionId01, GUEST1_USERNAME, "");
        userCantTakeSystemAction(GUEST1_USERNAME, publishSessionTemplateEndpoint, publishSessionTemplateBody);
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToUser1" }, groups = { "viewSession01FirstTime" })
    public void testAdminCanViewSessionTemplate01AfterPublish() {
        testUserDescribe(ADMIN1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, List.of(sessionTemplateId01));
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToUser1" }, groups = { "viewSession01FirstTime" })
    public void testUser1CanViewSessionTemplate01AfterPublish() {
        testUserDescribe(USER1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, List.of(sessionTemplateId01));
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToUser1" }, groups = { "viewSession01FirstTime" })
    public void testUser2CantViewSessionTemplate01AfterPublish() {
        testUserDescribe(USER2_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToUser1" }, groups = { "viewSession01FirstTime" })
    public void testGuest1CantViewSessionTemplate01AfterPublish() {
        userCantTakeSystemAction(GUEST1_USERNAME, describeSessionTemplatesEndpoint, EMPTY_JSON);
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToUser1" })
    public void testUser1CanCreateSessionAfterSessionTemplate01Publish() {
        sessionId02 = userCanCreateSession(USER1_USERNAME, sessionTemplateId01);
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToUser1" })
    public void testUser2CantCreateSessionAfterSessionTemplate01Publish() {
        userCantCreateSession(USER2_USERNAME, sessionTemplateId01);
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToUser1" })
    public void testGuest1CantCreateSession01AfterSessionTemplate01Publish() {
        String createSessionBody = String.format(CREATE_SESSION_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(GUEST1_USERNAME, createSessionEndpoint, createSessionBody);
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" }, groups = { "secondSessionCreation "})
    public void testAdminCanViewSession01andSession02() {
        testUserDescribe(ADMIN1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, List.of(sessionId01, sessionId02));
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" }, groups = { "secondSessionCreation "})
    public void testUser1CanViewSession02() {
        testUserDescribe(USER1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, List.of(sessionId02));
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" }, groups = { "secondSessionCreation "})
    public void testUser2CantViewAnySessions() {
        testUserDescribe(USER2_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" }, groups = { "secondSessionCreation "})
    public void testGuest1CantViewAnySessions() {
        testUserDescribe(GUEST1_USERNAME, describeSessionEndpoint, SESSIONS_KEY, DESCRIBE_SESSIONS_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" }, groups = { "secondSessionCreation "})
    public void testAdmin1CanConnectToSession02() {
        waitUntilSessionIsReady(ADMIN1_USERNAME, sessionId02);
        userCanConnectToSession(ADMIN1_USERNAME, sessionId02);
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" }, groups = { "secondSessionCreation "})
    public void testUser1CanConnectToSession02() {
        waitUntilSessionIsReady(USER1_USERNAME, sessionId02);
        userCanConnectToSession(USER1_USERNAME, sessionId02);
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" },  groups = { "secondSessionCreation "})
    public void testUser2CantConnectToSession02() {
        userCantConnectToSession(USER2_USERNAME, sessionId02);
    }

    @Test(dependsOnMethods = { "testUser1CanCreateSessionAfterSessionTemplate01Publish" }, groups = { "secondSessionCreation "})
    public void testGuest1CantConnectToSession02() {
        userCantConnectToSession(GUEST1_USERNAME, sessionId02);
    }

    @Test(dependsOnGroups = { "secondSessionCreation "}, groups = {"session01DeletionChecks" })
    public void testUser1CantDeleteSession01() {
        cantDeleteSession(USER1_USERNAME, sessionId01, ADMIN1_USERNAME);
    }

    @Test(dependsOnGroups = { "secondSessionCreation "}, groups = {"session01DeletionChecks" })
    public void testUser2CantDeleteSession01() {
        cantDeleteSession(USER2_USERNAME, sessionId01, ADMIN1_USERNAME);
    }

    @Test(dependsOnGroups = { "secondSessionCreation "}, groups = {"session01DeletionChecks" })
    public void testGuest1CantDeleteSession01() {
        String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId01, ADMIN1_USERNAME);
        userCantTakeSystemAction(GUEST1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
    }

    @Test(dependsOnGroups = { "secondSessionCreation ", "session01DeletionChecks"})
    public void testAdminCanDeleteSession01() {
        waitUntilSessionIsReady(ADMIN1_USERNAME, sessionId01);
        String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId01, ADMIN1_USERNAME);
        canDeleteResource(ADMIN1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
        sessionId01 = null;
    }

    @Test(dependsOnGroups = { "session01DeletionChecks" }, groups = { "session02DeletionChecks" })
    public void testUser2CantDeleteSession02() {
        cantDeleteSession(USER2_USERNAME, sessionId02, USER1_USERNAME);
    }

    @Test(dependsOnGroups = { "session01DeletionChecks" }, groups = { "session02DeletionChecks" })
    public void testGuest1CantDeleteSession02() {
        String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId02, ADMIN1_USERNAME);
        userCantTakeSystemAction(GUEST1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
    }

    @Test(dependsOnGroups = { "session02DeletionChecks"})
    public void testUser1CanDeleteSession02() {
        waitUntilSessionIsReady(USER1_USERNAME, sessionId02);
        String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId02, USER1_USERNAME);
        canDeleteResource(USER1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
        sessionId02 = null;
    }

    @Test(dependsOnMethods = { "testAdminCanDeleteSession01" }, dependsOnGroups = { "viewSession01FirstTime" })
    public void testAdminCanUnpublishSessionTemplate01ToUser1() {
        userCanUnpublishSessionTemplate(ADMIN1_USERNAME, sessionTemplateId01, List.of(USER1_USERNAME), List.of(GROUP1_NAME));
    }

    @Test(dependsOnMethods = { "testAdminCanDeleteSession01" }, dependsOnGroups = { "viewSession01FirstTime" })
    public void testUser1CantUnpublishSessionTemplate01ToUser1() {
        String unpublishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionId01, USER1_USERNAME, "");
        userCantTakeSystemAction(USER1_USERNAME, unpublishSessionTemplateEndpoint, unpublishSessionTemplateBody);
    }

    @Test(dependsOnMethods = { "testAdminCanDeleteSession01" }, dependsOnGroups = { "viewSession01FirstTime" })
    public void testUser2CantUnpublishSessionTemplate01ToUser1() {
        String unpublishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionId01, USER2_USERNAME, "");
        userCantTakeSystemAction(USER2_USERNAME, unpublishSessionTemplateEndpoint, unpublishSessionTemplateBody);
    }

    @Test(dependsOnMethods = { "testAdminCanDeleteSession01" }, dependsOnGroups = { "viewSession01FirstTime" })
    public void testGuest1CantUnpublishSessionTemplate01ToUser1() {
        String unpublishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionId01, GUEST1_USERNAME, "");
        userCantTakeSystemAction(GUEST1_USERNAME, unpublishSessionTemplateEndpoint, unpublishSessionTemplateBody);
    }

    @Test(dependsOnMethods = { "testAdminCanUnpublishSessionTemplate01ToUser1" })
    public void testAdminCanPublishSessionTemplate01ToGroup1() {
        userCanPublishSessionTemplate(ADMIN1_USERNAME, sessionTemplateId01, Collections.emptyList(), List.of(GROUP1_NAME));
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToGroup1" }, groups = { "viewSessionTemplate01AfterPublishToGroup1" })
    public void testAdmin1CanViewSessionTemplate01AfterPublishToGroup1() {
        testUserDescribe(ADMIN1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, List.of(sessionTemplateId01));
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToGroup1" }, groups = { "viewSessionTemplate01AfterPublishToGroup1" })
    public void testUser1CantViewSessionTemplate01AfterPublishToGroup1() {
        testUserDescribe(USER1_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, Collections.emptyList());
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToGroup1" }, groups = { "viewSessionTemplate01AfterPublishToGroup1" })
    public void testUser2CanViewSessionTemplate01AfterPublishToGroup1() {
        testUserDescribe(USER2_USERNAME, describeSessionTemplatesEndpoint, SESSION_TEMPLATES_KEY, EMPTY_JSON, List.of(sessionTemplateId01));
    }

    @Test(dependsOnMethods = { "testAdminCanPublishSessionTemplate01ToGroup1" }, groups = { "viewSessionTemplate01AfterPublishToGroup1" })
    public void testGuest1CantViewSessionTemplate01AfterPublishToGroup1() {
        userCantTakeSystemAction(GUEST1_USERNAME, describeSessionTemplatesEndpoint, EMPTY_JSON);
    }

    @Test(dependsOnGroups = { "viewSessionTemplate01AfterPublishToGroup1" })
    public void testUser1CantCreateSessionAfterSessionTemplate01Republish() {
        userCantCreateSession(USER1_USERNAME, sessionTemplateId01);
    }

    @Test(dependsOnGroups = { "viewSessionTemplate01AfterPublishToGroup1" })
    public void testUser2CanCreateSessionAfterSessionTemplate01Republish() {
        sessionId03 = userCanCreateSession(USER2_USERNAME, sessionTemplateId01);
    }

    @Test(dependsOnGroups = { "viewSessionTemplate01AfterPublishToGroup1" })
    public void testGuest1CantCreateSessionAfterSessionTemplate01Republish() {
        String createSessionBody = String.format(CREATE_SESSION_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(GUEST1_USERNAME, createSessionEndpoint, createSessionBody);
    }

    @Test(dependsOnMethods = { "testUser2CanCreateSessionAfterSessionTemplate01Republish" }, groups = { "thirdSessionViewChecks" })
    public void testAdmin1CanConnectToSession03() {
        waitUntilSessionIsReady(ADMIN1_USERNAME, sessionId03);
        userCanConnectToSession(ADMIN1_USERNAME, sessionId03);
    }

    @Test(dependsOnMethods = { "testUser2CanCreateSessionAfterSessionTemplate01Republish" }, groups = { "thirdSessionViewChecks" })
    public void testUser1CantConnectToSession03() {
        userCantConnectToSession(USER1_USERNAME, sessionId03);
    }

    @Test(dependsOnMethods = { "testUser2CanCreateSessionAfterSessionTemplate01Republish" }, groups = { "thirdSessionViewChecks" })
    public void testUser2CanConnectToSession03() {
        waitUntilSessionIsReady(USER2_USERNAME, sessionId03);
        userCanConnectToSession(USER2_USERNAME, sessionId03);
    }

    @Test(dependsOnMethods = { "testUser2CanCreateSessionAfterSessionTemplate01Republish" }, groups = { "thirdSessionViewChecks" })
    public void testGuest1CantConnectToSession03() {
        userCantConnectToSession(GUEST1_USERNAME, sessionId03);
    }

    @Test(dependsOnGroups = { "thirdSessionViewChecks" }, groups = { "thirdSessionDeletionChecks" })
    public void testUser1CantDeleteSession03() {
        cantDeleteSession(USER1_USERNAME, sessionId03, USER2_USERNAME);
    }

    @Test(dependsOnGroups = { "thirdSessionViewChecks" }, groups = { "thirdSessionDeletionChecks" })
    public void testGuest1CantDeleteSession03() {
        String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId03, ADMIN1_USERNAME);
        userCantTakeSystemAction(GUEST1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
    }

    @Test(dependsOnGroups = { "thirdSessionDeletionChecks" })
    public void testUser2CanDeleteSession03() {
        waitUntilSessionIsReady(USER2_USERNAME, sessionId03);
        String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId03, USER2_USERNAME);
        canDeleteResource(USER2_USERNAME, deleteSessionEndpoint, deleteSessionBody);
        sessionId03 = null;
    }

    @Test(dependsOnMethods = { "testUser2CanDeleteSession03" }, groups = { "userGroupDeletionChecks" } )
    public void testUser1CantDeleteUserGroup() {
        String deleteUserGroupsBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(USER1_USERNAME, deleteUserGroupsEndpoint, deleteUserGroupsBody);
    }

    @Test(dependsOnMethods = { "testUser2CanDeleteSession03" }, groups = { "userGroupDeletionChecks" } )
    public void testUser2CantDeleteUserGroup() {
        String deleteUserGroupsBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(USER2_USERNAME, deleteUserGroupsEndpoint, deleteUserGroupsBody);
    }

    @Test(dependsOnMethods = { "testUser2CanDeleteSession03" }, groups = { "userGroupDeletionChecks" } )
    public void testGuest1CantDeleteUserGroup() {
        String deleteUserGroupsBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        userCantTakeSystemAction(GUEST1_USERNAME, deleteUserGroupsEndpoint, deleteUserGroupsBody);
    }

    @Test(dependsOnGroups = { "userGroupDeletionChecks" })
    public void testAdminCanDeleteUserGroup() {
        String deleteUsersGroupsBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        canDeleteResource(ADMIN1_USERNAME, deleteUserGroupsEndpoint, deleteUsersGroupsBody);
    }

    @Test(groups = { "sessionTemplate01DeletionChecks" } )
    public void testUser1CantDeleteSessionTemplate01() {
        String deleteSessionTemplateBody = String.format(DELETE_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER1_USERNAME, deleteSessionTemplateEndpoint, deleteSessionTemplateBody);
    }

    @Test(groups = { "sessionTemplate01DeletionChecks" } )
    public void testUser2CantDeleteSessionTemplate01() {
        String deleteSessionTemplateBody = String.format(DELETE_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(USER2_USERNAME, deleteSessionTemplateEndpoint, deleteSessionTemplateBody);
    }

    @Test(groups = { "sessionTemplate01DeletionChecks" } )
    public void testGuest1CantDeleteSessionTemplate01() {
        String deleteSessionTemplateBody = String.format(DELETE_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        userCantTakeSystemAction(GUEST1_USERNAME, deleteSessionTemplateEndpoint, deleteSessionTemplateBody);
    }

    @Test(dependsOnMethods = { "testUser2CanCreateSessionAfterSessionTemplate01Republish" } , dependsOnGroups = { "sessionTemplate01DeletionChecks" } )
    public void testAdminCanDeleteSessionTemplate01() {
        String deleteSessionTemplateBody = String.format(DELETE_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
        canDeleteResource(ADMIN1_USERNAME, deleteSessionTemplateEndpoint, deleteSessionTemplateBody);
        sessionTemplateId01 = null;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        if (sessionTemplateId01 != null) {
            log.info("SessionTemplate01 with id {} never got deleted. Deleting it now.", sessionTemplateId01);
            String deleteSessionTemplateBody = String.format(DELETE_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId01);
            canDeleteResource(ADMIN1_USERNAME, deleteSessionTemplateEndpoint, deleteSessionTemplateBody);
        }
        if (sessionId01 != null) {
            log.info("Session01 with id {} never got deleted. Deleting it now.", sessionId01);
            waitUntilSessionIsReady(ADMIN1_USERNAME, sessionId01);
            String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId01, ADMIN1_USERNAME);
            canDeleteResource(ADMIN1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
        }
        if (sessionId02 != null) {
            log.info("Session02 with id {} never got deleted. Deleting it now.", sessionId02);
            waitUntilSessionIsReady(ADMIN1_USERNAME, sessionId02);
            String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId02, USER1_USERNAME);
            canDeleteResource(ADMIN1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
        }
        if (sessionId03 != null) {
            log.info("Session03 with id {} never got deleted. Deleting it now.", sessionId03);
            waitUntilSessionIsReady(ADMIN1_USERNAME, sessionId03);
            String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId03, USER2_USERNAME);
            canDeleteResource(ADMIN1_USERNAME, deleteSessionEndpoint, deleteSessionBody);
        }
        String deleteUserGroupsBody = String.format(DELETE_USER_GROUPS_JSON_FORMAT_STRING, GROUP1_NAME);
        canDeleteResource(ADMIN1_USERNAME, deleteUserGroupsEndpoint, deleteUserGroupsBody);
        httpClient.close();
    }

    private void waitUntilSessionIsReady(String username, String sessionId) {
        assertNotNull(sessionId);
        int attempts = NUMBER_OF_RETRIES;
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            while (attempts > 0) {
                HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, describeSessionEndpoint, DESCRIBE_SESSIONS_JSON, true);
                log.info("Checked describeSessions for session readiness with body {} and got response: {}", DESCRIBE_SESSIONS_JSON, response);
                assertNotNull(response);
                if (response.containsKey(SESSIONS_KEY) && response.get(SESSIONS_KEY) != null && response.get(SESSIONS_KEY).getClass() == ArrayList.class) {
                    ArrayList<Object> sessions = (ArrayList<Object>)response.get(SESSIONS_KEY);
                    List<Object> sessionsList = sessions.stream().filter(s -> {
                        assertEquals(s.getClass(), LinkedHashMap.class);
                        LinkedHashMap<String, Object> sessionMap = (LinkedHashMap<String, Object>)s;
                        return sessionMap.get(ID_KEY).equals(sessionId);
                    }).toList();
                    assertEquals(sessionsList.size(), 1, "Newly created session " + sessionId + " is missing from describeSessions response");
                    LinkedHashMap<String, Object> session = (LinkedHashMap<String, Object>)sessionsList.get(0);
                    if (session.containsKey(STATE_KEY) && session.get(STATE_KEY) != null && session.get(STATE_KEY).equals(READY_STATE)) {
                        log.info("Session {} is ready", sessionId);
                        return;
                    }
                    attempts--;
                    log.info("Session {} is not ready yet. {} attempts left. Sleeping for {} ms", sessionId, attempts, TIME_BETWEEN_RETRIES_MS);
                    Thread.sleep(TIME_BETWEEN_RETRIES_MS);
                }
            }
            log.warn("Session {} is not ready after {} attempts", sessionId, NUMBER_OF_RETRIES);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void canDeleteResource(String username, String endpoint, String requestBody) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HashMap<String, Object> response = authServerUtils.makeHandlerDeleteCall(authorizationHeader, httpClient, endpoint, requestBody, true);
            log.info("Called deleteSession with body {} and got response: {}", requestBody, response);
            assertNotNull(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cantDeleteSession(String username, String sessionId, String owner) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            String deleteSessionBody = String.format(DELETE_SESSION_JSON_FORMAT_STRING, sessionId, owner);
            HashMap<String, Object> response = authServerUtils.makeHandlerDeleteCall(authorizationHeader, httpClient, deleteSessionEndpoint, deleteSessionBody, true);
            log.info("Called deleteSession with body {} and got response: {}", deleteSessionBody, response);
            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(SUCCESSFUL_LIST_KEY));
            assertNotNull(response.get(UNSUCCESSFUL_LIST_KEY));
            assertEquals(response.get(UNSUCCESSFUL_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<Object> unsuccessfulList = (ArrayList<Object>)response.get(UNSUCCESSFUL_LIST_KEY);
            assertEquals(unsuccessfulList.size(), 1);
            assertSame(unsuccessfulList.get(0).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> unsuccessfulSession = (LinkedHashMap<String, Object>)unsuccessfulList.get(0);
            assertTrue(unsuccessfulSession.containsKey(SESSION_ID_KEY));
            assertTrue(unsuccessfulSession.containsKey(FAILURE_REASONS_KEY));
            assertEquals(unsuccessfulSession.get(SESSION_ID_KEY), sessionId);
            assertEquals(unsuccessfulSession.get(FAILURE_REASONS_KEY).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> failureReasons = (LinkedHashMap<String, Object>)unsuccessfulSession.get(FAILURE_REASONS_KEY);
            assertTrue(failureReasons.containsKey(UNAUTHORIZED_KEY));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void userCanConnectToSession(String user1Username, String sessionId) {
        String connectSessionEndpoint = String.format(connectSessionEndpointFormatString, sessionId);
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(user1Username);
        try {
            HashMap<String, Object> response = authServerUtils.makeHandlerPostCallNoEntity(authorizationHeader, httpClient, connectSessionEndpoint, true);
            log.info("Called connectSession endpoint {} and got response: {}", connectSessionEndpoint, response);
            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(SESSION_KEY));
            assertEquals(response.get(SESSION_KEY).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> session = (LinkedHashMap<String, Object>)response.get(SESSION_KEY);
            assertTrue(session.containsKey(ID_KEY));
            assertEquals(session.get(ID_KEY).toString(), sessionId);

            assertTrue(response.containsKey(CONNECTION_TOKEN_KEY));
            assertNotNull(response.get(CONNECTION_TOKEN_KEY));
            log.info("User {} successfully connected to session {}", user1Username, sessionId);
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + connectSessionEndpoint + " failed. ", e);
        }
    }

    private void userCantConnectToSession(String username, String sessionId) {
        String connectSessionEndpoint = String.format(connectSessionEndpointFormatString, sessionId);
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HashMap<String, Object> response = authServerUtils.makeHandlerPostCallNoEntity(authorizationHeader, httpClient, connectSessionEndpoint, false);
            assertNull(response, "User " + username + " should not have been able to connect to session " + sessionId);
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + connectSessionEndpoint + " failed. ", e);
        }
    }

    private String userCanCreateSession(String username, String sessionTemplateId) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            String createSessionBody = String.format(CREATE_SESSION_JSON_FORMAT_STRING, sessionTemplateId);

            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, createSessionEndpoint, createSessionBody, true);
            log.info("Called createSession with body {} and got response: {}", createSessionBody, response);

            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertNull(response.get(UNSUCCESSFUL_LIST_KEY));
            assertTrue(response.containsKey(SUCCESSFUL_LIST_KEY));
            assertEquals(response.get(SUCCESSFUL_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<Object> successfulList = (ArrayList<Object>)response.get(SUCCESSFUL_LIST_KEY);
            assertEquals(successfulList.size(), 1);

            assertNotNull(successfulList.get(0));
            assertEquals(successfulList.get(0).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> session = (LinkedHashMap<String, Object>)successfulList.get(0);
            assertTrue(session.containsKey(ID_KEY));
            assertTrue(session.containsKey(NAME_KEY));
            assertTrue(session.containsKey(OWNER_KEY));

            assertEquals(session.get(NAME_KEY).toString(), SESSION_NAME);
            assertEquals(session.get(OWNER_KEY).toString(), username);

            log.info("Successfully created session with id: {}", session.get(ID_KEY));
            return session.get(ID_KEY).toString();
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + createSessionEndpoint + " failed.", e);
        }
    }

    private void userCantCreateSession(String username, String sessionTemplateId) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            String createSessionBody = String.format(CREATE_SESSION_JSON_FORMAT_STRING, sessionTemplateId);
            // We expect them to be authorized because Users have permission to call /createSession. However, the request will contain elements in the Unsuccessful list.
            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, createSessionEndpoint, createSessionBody, true);
            log.info("Called createSession with body {} and got response: {}", createSessionBody, response);
            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(SUCCESSFUL_LIST_KEY));
            assertNull(response.get(SUCCESSFUL_LIST_KEY));
            assertTrue(response.containsKey(UNSUCCESSFUL_LIST_KEY));
            assertEquals(response.get(UNSUCCESSFUL_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<Object> unsuccessfulList = (ArrayList<Object>)response.get(UNSUCCESSFUL_LIST_KEY);
            assertEquals(unsuccessfulList.size(), 1, "User " + username + " should not be able to create session with template " + sessionTemplateId);
            assertEquals(unsuccessfulList.get(0).getClass(), LinkedHashMap.class);
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + createSessionEndpoint + " failed.", e);
        }
    }

    private void userCantTakeSystemAction(String username, String endpoint, String requestBody) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, endpoint, requestBody, false);
            assertNull(response, "User " + username + " should not be able to take action on " + endpoint);
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + endpoint + " failed. ", e);
        }
    }

    private String userCanCreateSessionTemplate(String username) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, createSessionTemplateEndpoint, CREATE_SESSION_TEMPLATE_JSON, true);
            log.info("Called createSessionTemplate with body {} and got response: {}", CREATE_SESSION_TEMPLATE_JSON, response);

            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(SESSION_TEMPLATE_KEY));
            assertEquals(response.get(SESSION_TEMPLATE_KEY).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> sessionTemplate = (LinkedHashMap<String, Object>)response.get(SESSION_TEMPLATE_KEY);
            assertTrue(sessionTemplate.containsKey(ID_KEY));
            assertTrue(sessionTemplate.containsKey(NAME_KEY));
            assertTrue(sessionTemplate.containsKey(CREATED_BY_KEY));

            assertEquals(SESSION_TEMPLATE_NAME, sessionTemplate.get(NAME_KEY).toString());
            assertEquals(username, sessionTemplate.get(CREATED_BY_KEY).toString());
            log.info("Successfully created SessionTemplate with ID {}", sessionTemplate.get(ID_KEY));
            return sessionTemplate.get(ID_KEY).toString();
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + createSessionTemplateEndpoint + " failed.", e);
        }
    }

    private String userCanEditSessionTemplate(String username, String requestBody) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HashMap<String,Object> response = authServerUtils.makeHandlerPutCall(authorizationHeader, httpClient, editSessionTemplateEndpoint, requestBody, true);
            log.info("Called editSessionTemplate with body {} and got response: {}", requestBody, response);

            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(SESSION_TEMPLATE_KEY));
            assertEquals(response.get(SESSION_TEMPLATE_KEY).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> sessionTemplate = (LinkedHashMap<String, Object>)response.get(SESSION_TEMPLATE_KEY);
            assertTrue(sessionTemplate.containsKey(ID_KEY));
            assertTrue(sessionTemplate.containsKey(NAME_KEY));
            assertTrue(sessionTemplate.containsKey(CREATED_BY_KEY));

            assertEquals(SESSION_TEMPLATE_NAME, sessionTemplate.get(NAME_KEY).toString());
            assertEquals(username, sessionTemplate.get(CREATED_BY_KEY).toString());
            log.info("Successfully edited SessionTemplate with ID {}", sessionTemplate.get(ID_KEY));
            return sessionTemplate.get(ID_KEY).toString();
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + editSessionTemplateEndpoint + " failed.", e);
        }
    }

    private void userCanPublishSessionTemplate(String username, String sessionTemplateId, List<String> usersList, List<String> groupsList) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);

        String usersListString = usersList.isEmpty() ? EMPTY_STRING : (ESCAPED_QUOTE + String.join(LIST_WITH_QUOTES_DELIMITER, usersList) + ESCAPED_QUOTE);
        String groupsListString = groupsList.isEmpty() ? EMPTY_STRING : (ESCAPED_QUOTE + String.join(LIST_WITH_QUOTES_DELIMITER, groupsList) + ESCAPED_QUOTE);

        String publishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId, usersListString, groupsListString);

        try {
            HashMap<String, Object> response = authServerUtils.makeHandlerPutCall(authorizationHeader, httpClient, publishSessionTemplateEndpoint, publishSessionTemplateBody, true);
            log.info("Called publishSessionTemplate with body {} and got response: {}", publishSessionTemplateBody, response);
            assertNotNull(response);

            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));

            assertTrue(response.containsKey(UNSUCCESSFUL_USERS_LIST_KEY));
            assertEquals(response.get(UNSUCCESSFUL_USERS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<String> unsuccessfulUsersList = (ArrayList<String>)response.get(UNSUCCESSFUL_USERS_LIST_KEY);
            assertEquals(unsuccessfulUsersList.size(), 0, "The list of unsuccessful users should be empty");

            assertTrue(response.containsKey(UNSUCCESSFUL_GROUPS_LIST_KEY));
            assertEquals(response.get(UNSUCCESSFUL_GROUPS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<String> unsuccessfulGroupsList = (ArrayList<String>)response.get(UNSUCCESSFUL_GROUPS_LIST_KEY);
            assertEquals(unsuccessfulGroupsList.size(), 0, "The list of unsuccessful groups should be empty");

            assertTrue(response.containsKey(SUCCESSFUL_USERS_LIST_KEY));
            assertEquals(response.get(SUCCESSFUL_USERS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<Object> successfulUsersList = (ArrayList<Object>) response.get(SUCCESSFUL_USERS_LIST_KEY);
            assertEquals(successfulUsersList.size(), usersList.size(), "The list of successful users should be the same size as the list of users");

            assertTrue(response.containsKey(SUCCESSFUL_GROUPS_LIST_KEY));
            assertEquals(response.get(SUCCESSFUL_GROUPS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<Object> successfulGroupsList = (ArrayList<Object>) response.get(SUCCESSFUL_GROUPS_LIST_KEY);
            assertEquals(successfulGroupsList.size(), groupsList.size(), "The list of successful groups should be the same size as the list of groups");

            for (String user : usersList) {
                assertTrue(successfulUsersList.stream().anyMatch(resource ->
                        resource.toString().equals(user)
                ), "Not able to find user " + user + " in successful users list");
            }

            for (String group : groupsList) {
                assertTrue(successfulGroupsList.stream().anyMatch(resource ->
                        resource.toString().equals(group)
                ),  "Not able to find group " + group + " in successful groups list");
            }
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + publishSessionTemplateEndpoint + " failed.", e);
        }
    }

    private void userCanUnpublishSessionTemplate(String username, String sessionTemplateId, List<String> usersList, List<String> groupsList) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);

        String usersListString = usersList.isEmpty() ? EMPTY_STRING : (ESCAPED_QUOTE + String.join(LIST_WITH_QUOTES_DELIMITER, usersList) + ESCAPED_QUOTE);
        String groupsListString = groupsList.isEmpty() ? EMPTY_STRING : (ESCAPED_QUOTE + String.join(LIST_WITH_QUOTES_DELIMITER, groupsList) + ESCAPED_QUOTE);

        String unpublishSessionTemplateBody = String.format(PUBLISH_UNPUBLISH_SESSION_TEMPLATE_JSON_FORMAT_STRING, sessionTemplateId, usersListString, groupsListString);

        try {
            HashMap<String, Object> response = authServerUtils.makeHandlerPutCall(authorizationHeader, httpClient, unpublishSessionTemplateEndpoint, unpublishSessionTemplateBody, true);
            log.info("Called unpublishSessionTemplateEndpoint with body {} and got response: {}", unpublishSessionTemplateBody, response);
            assertNotNull(response);

            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));

            assertTrue(response.containsKey(UNSUCCESSFUL_USERS_LIST_KEY));
            assertNull(response.get(UNSUCCESSFUL_USERS_LIST_KEY));

            assertTrue(response.containsKey(UNSUCCESSFUL_GROUPS_LIST_KEY));
            assertNull(response.get(UNSUCCESSFUL_GROUPS_LIST_KEY));

            assertTrue(response.containsKey(SUCCESSFUL_USERS_LIST_KEY));
            assertEquals(response.get(SUCCESSFUL_USERS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<Object> successfulUsersList = (ArrayList<Object>) response.get(SUCCESSFUL_USERS_LIST_KEY);
            assertEquals(successfulUsersList.size(), usersList.size(), "The list of successful users should be the same size as the list of users");

            assertTrue(response.containsKey(SUCCESSFUL_GROUPS_LIST_KEY));
            assertEquals(response.get(SUCCESSFUL_GROUPS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<Object> successfulGroupsList = (ArrayList<Object>) response.get(SUCCESSFUL_GROUPS_LIST_KEY);
            assertEquals(successfulGroupsList.size(), groupsList.size(), "The list of successful groups should be the same size as the list of groups");

            for (String user : usersList) {
                assertTrue(successfulUsersList.stream().anyMatch(resource ->
                        resource.toString().equals(user)
                ), "Not able to find user " + user + " in successful users list");
            }

            for (String group : groupsList) {
                assertTrue(successfulGroupsList.stream().anyMatch(resource ->
                        resource.toString().equals(group)
                ),  "Not able to find group " + group + " in successful groups list");
            }
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + unpublishSessionTemplateEndpoint + " failed.", e);
        }
    }

    private void testUserDescribe(String username, String endpoint, String resourceKey, String body, List<String> expectedResourceIds) {
        testUserDescribeWithOptionalIds(username, endpoint, resourceKey, body, Optional.of(expectedResourceIds));
    }

    private void testUserDescribe(String username, String endpoint, String resourceKey, String body) {
        testUserDescribeWithOptionalIds(username, endpoint, resourceKey, body, Optional.empty());
    }

    private void testUserDescribeWithOptionalIds(String username, String endpoint, String resourceKey, String body, Optional<List<String>> expectedResourceIds) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, endpoint, body, true);
            log.info("Called {}  with body {} and got response: {}", endpoint, body, response);

            assertNotNull(response);
            assertTrue(response.containsKey(resourceKey));
            assertNotNull(response.get(resourceKey));
            assertEquals(response.get(resourceKey).getClass(), ArrayList.class);
            ArrayList<Object> describeResults = ((ArrayList<Object>)response.get(resourceKey));
            log.info("Describe Results: {}", describeResults);

            if (expectedResourceIds.isPresent()) {
                assertEquals(describeResults.size(), expectedResourceIds.get().size(), "Describe results size does not match");
                for (String expectedResourceId : expectedResourceIds.get()) {
                    assertTrue(describeResults.stream().anyMatch(resource -> {
                        if (resource instanceof LinkedHashMap) {
                            LinkedHashMap<String, Object> resourceMap = (LinkedHashMap<String, Object>) resource;
                            String idKey = ID_KEY;
                            if (USERS_KEY.equals(resourceKey)) {
                                idKey = USER_ID_KEY;
                            } else if (USER_GROUPS_KEY.equals(resourceKey)) {
                                idKey = USER_GROUP_ID_KEY;
                            }
                            return resourceMap.get(idKey).toString().equals(expectedResourceId);
                        }
                        return false;
                    }), "Could not find resource with id " + expectedResourceId + " in describe results.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + endpoint + " failed. ", e);
        }
    }

    private void testCreateUserGroup(String username, String userGroupId) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            String createUserGroupBody = String.format(CREATE_USER_GROUP_JSON_FORMAT_STRING, userGroupId);
            HashMap<String,Object> response = authServerUtils.makeHandlerPostCall(authorizationHeader, httpClient, createUserGroupEndpoint, createUserGroupBody, true);
            log.info("Called createUserGroup with body {} and got response: {}", createUserGroupBody, response);

            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(SUCCESSFUL_USERS_LIST_KEY));
            assertNull(response.get(SUCCESSFUL_USERS_LIST_KEY));
            assertTrue(response.containsKey(UNSUCCESSFUL_USERS_LIST_KEY));
            assertNull(response.get(UNSUCCESSFUL_USERS_LIST_KEY));
            assertTrue(response.containsKey(SUCCESSFUL_SESSION_TEMPLATE_LIST_KEY));
            assertNull(response.get(SUCCESSFUL_SESSION_TEMPLATE_LIST_KEY));
            assertTrue(response.containsKey(UNSUCCESSFUL_SESSION_TEMPLATE_LIST_KEY));
            assertNull(response.get(UNSUCCESSFUL_SESSION_TEMPLATE_LIST_KEY));
            assertTrue(response.containsKey(USER_GROUP_KEY));
            assertEquals(response.get(USER_GROUP_KEY).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> userGroup = (LinkedHashMap<String, Object>) response.get(USER_GROUP_KEY);
            assertTrue(userGroup.containsKey(USER_GROUP_ID_KEY));
            assertEquals(userGroupId, userGroup.get(USER_GROUP_ID_KEY));
            log.info("Successfully created userGroup with ID {}", userGroup.get(USER_GROUP_ID_KEY));
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + createUserGroupEndpoint + " failed.", e);
        }
    }

    private void testEditUserGroup(String username, String userGroupId, List<String> usersList) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            String usersListString = usersList.isEmpty() ? EMPTY_STRING : (ESCAPED_QUOTE + String.join(LIST_WITH_QUOTES_DELIMITER, usersList) + ESCAPED_QUOTE);
            String editUserGroupBody = String.format(EDIT_USER_GROUP_JSON_FORMAT_STRING, userGroupId, usersListString);
            HashMap<String,Object> response = authServerUtils.makeHandlerPutCall(authorizationHeader, httpClient, editUserGroupEndpoint, editUserGroupBody, true);
            log.info("Called editUserGroup with body {} and got response: {}", editUserGroupBody, response);

            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(USER_GROUP_KEY));
            assertEquals(response.get(USER_GROUP_KEY).getClass(), LinkedHashMap.class);
            LinkedHashMap<String, Object> userGroup = (LinkedHashMap<String, Object>) response.get(USER_GROUP_KEY);
            assertTrue(userGroup.containsKey(USER_GROUP_ID_KEY));
            assertEquals(userGroupId, userGroup.get(USER_GROUP_ID_KEY));
            log.info("Successfully edited userGroup with ID {}", userGroup.get(USER_GROUP_ID_KEY));
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + editUserGroupEndpoint + " failed.", e);
        }
    }

    private void cantEditUserGroup(String username, String userGroupId, List<String> usersList) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            String usersListString = usersList.isEmpty() ? EMPTY_STRING : (ESCAPED_QUOTE + String.join(LIST_WITH_QUOTES_DELIMITER, usersList) + ESCAPED_QUOTE);
            String editUserGroupBody = String.format(EDIT_USER_GROUP_JSON_FORMAT_STRING, userGroupId, usersListString);
            HashMap<String,Object> response = authServerUtils.makeHandlerPutCall(authorizationHeader, httpClient, editUserGroupEndpoint, editUserGroupBody, false);
            assertNull(response, "User " + username + " should not be able to take action on " + editUserGroupEndpoint);
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + editUserGroupEndpoint + " failed.", e);
        }
    }

    private void testImportUsers(String username, List<String> userIds) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HttpEntity entity = MultipartEntityBuilder
                    .create()
                    .addBinaryBody("File", IMPORT_FILE_CONTENT.getBytes(), ContentType.DEFAULT_BINARY, "file.csv")
                    .addTextBody("OverwriteExistingUsers", "true")
                    .addTextBody("OverwriteGroups", "false")
                    .build();
            HashMap<String,Object> response = authServerUtils.makeHandlerMultipartPostCall(authorizationHeader, httpClient, importUsersEndpoint, entity, true);
            log.info("Called importUsers and got response: {}", response);

            assertNotNull(response);
            assertTrue(response.containsKey(ERROR_KEY));
            assertNull(response.get(ERROR_KEY));
            assertTrue(response.containsKey(UNSUCCESSFUL_USERS_LIST_KEY));
            assertEquals(response.get(UNSUCCESSFUL_USERS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<String> unsuccessfulUsersList = (ArrayList<String>) response.get(UNSUCCESSFUL_USERS_LIST_KEY);
            assertTrue(unsuccessfulUsersList.isEmpty());
            assertTrue(response.containsKey(SUCCESSFUL_USERS_LIST_KEY));
            assertEquals(response.get(SUCCESSFUL_USERS_LIST_KEY).getClass(), ArrayList.class);
            ArrayList<String> successfulUsersList = (ArrayList<String>) response.get(SUCCESSFUL_USERS_LIST_KEY);
            assertEquals(successfulUsersList.size(), userIds.size());
            assertTrue(successfulUsersList.containsAll(userIds));
            log.info("Successfully imported users");
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + importUsersEndpoint + " failed.", e);
        }
    }

    private void cantImportUsers(String username) {
        Header authorizationHeader = authServerUtils.getAuthorizationHeader(username);
        try {
            HttpEntity entity = MultipartEntityBuilder
                    .create()
                    .addBinaryBody("File", IMPORT_FILE_CONTENT.getBytes(), ContentType.DEFAULT_BINARY, "file.csv")
                    .addTextBody("OverwriteExistingUsers", "true")
                    .addTextBody("OverwriteGroups", "false")
                    .build();
            HashMap<String,Object> response = authServerUtils.makeHandlerMultipartPostCall(authorizationHeader, httpClient, importUsersEndpoint, entity, false);
            assertNull(response, "User " + username + " should not be able to take action on " + importUsersEndpoint);
        } catch (IOException e) {
            throw new RuntimeException("Connection to handler at " + importUsersEndpoint + " failed.", e);
        }
    }
}
