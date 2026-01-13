// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.services.AuthServerClientService;
import handler.model.User;
import handler.services.UserService;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DescribeUserInfoController.class)
@TestPropertySource(properties = {
        "jwt-login-username-claim-key=username",
        "jwt-display-name-claim-key=name",
        "jwt-role-claim-key=role",
        "default-role=User"
})
public class DescribeUserInfoControllerFromAuthServerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DescribeUserInfoController controller;

    @MockBean
    private UserService mockUserService;

    @MockBean
    private AuthServerClientService mockAuthServerClientService;

    @Value("${web-client-url}")
    private String origin;

    private final static String urlTemplate = "/describeUserInfo";
    private final static String testLoginUsername = "testLoginUsername";
    private final static String testUserDisplayName = "testUserDisplayName";
    private final static String testUserRole = "testUserRole";
    private final static String testUserValidRole = "Admin";
    private final static String testUserInvalidRole = "InvalidRole";
    private final static String testUserDefaultRole = "User";

    @BeforeEach
    public void setUp() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(testUser)
                .issuer("https://test-issuer.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);
        
        ReflectionTestUtils.setField(controller, "useAuthServerClaimsFromAccessToken", false);
        
        when(mockAuthorizationEngine.getRoles()).thenReturn(Arrays.asList(testUserValidRole, testUserDefaultRole, "Guest"));
        when(mockAuthorizationEngine.getDefaultUserRole()).thenReturn(testUserDefaultRole);
    }

    @Test
    public void testDescribeCurrentUser_WithAuthServerUpdate() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", testLoginUsername);
        userInfo.put("name", testUserDisplayName);
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(userInfo);

        User updatedUser = new User()
                .userId(testUser)
                .loginUsername(testLoginUsername)
                .displayName(testUserDisplayName)
                .role(testUserRole)
                .isDisabled(false);
        when(mockUserService.updateUser(eq(testUser), any(), eq(testUserDisplayName), any()))
                .thenReturn(updatedUser);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockAuthServerClientService).getUserInfo(any());
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName, null);
        verify(mockAuthorizationEngine).addUser(testUser, testLoginUsername, testUserDisplayName, testUserRole, false);
    }

    @Test
    public void testDescribeCurrentUser_AuthServerUpdateMissingClaims() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);

        Map<String, Object> userInfo = new HashMap<>();
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(userInfo);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockAuthServerClientService).getUserInfo(any());
        verify(mockUserService).updateUser(any(), any(), any(), any());
        verify(mockAuthorizationEngine, never()).addUser(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    public void testDescribeCurrentUser_AuthServerReturnsNull() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(null);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockAuthServerClientService).getUserInfo(any());
        verify(mockUserService, never()).updateUser(any(), any(), any(), any());
        verify(mockAuthorizationEngine, never()).addUser(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    public void testDescribeCurrentUser_UpdateUserFailure() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", testLoginUsername);
        userInfo.put("name", testUserDisplayName);
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(userInfo);

        when(mockUserService.updateUser(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Update failed"));

        mvc.perform(get(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                        .header(HttpHeaders.ORIGIN, origin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName, null);
        verify(mockAuthorizationEngine, never()).addUser(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    public void testDescribeCurrentUser_WithInvalidRole() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserDefaultRole);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", testLoginUsername);
        userInfo.put("name", testUserDisplayName);
        userInfo.put("role", testUserInvalidRole);
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(userInfo);

        User updatedUser = new User()
                .userId(testUser)
                .loginUsername(testLoginUsername)
                .displayName(testUserDisplayName)
                .role(testUserDefaultRole)
                .isDisabled(false);
        when(mockUserService.updateUser(eq(testUser), any(), eq(testUserDisplayName), eq(testUserDefaultRole)))
                .thenReturn(updatedUser);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserDefaultRole)));

        verify(mockAuthServerClientService).getUserInfo(any());
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName, testUserDefaultRole);
        verify(mockAuthorizationEngine).addUser(testUser, testLoginUsername, testUserDisplayName, testUserDefaultRole, false);
    }

    @Test
    public void testDescribeCurrentUser_WithValidRole() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserValidRole);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", testLoginUsername);
        userInfo.put("name", testUserDisplayName);
        userInfo.put("role", testUserValidRole);
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(userInfo);

        User updatedUser = new User()
                .userId(testUser)
                .loginUsername(testLoginUsername)
                .displayName(testUserDisplayName)
                .role(testUserValidRole)
                .isDisabled(false);
        when(mockUserService.updateUser(eq(testUser), any(), eq(testUserDisplayName), eq(testUserValidRole)))
                .thenReturn(updatedUser);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserValidRole)));

        verify(mockAuthServerClientService).getUserInfo(any());
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName, testUserValidRole);
        verify(mockAuthorizationEngine).addUser(testUser, testLoginUsername, testUserDisplayName, testUserValidRole, false);
    }

    @Test
    public void testDescribeCurrentUser_WithAuthServerClaimsFromToken() throws Exception {
        ReflectionTestUtils.setField(controller, "useAuthServerClaimsFromAccessToken", true);

        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", testLoginUsername);
        claims.put("name", testUserDisplayName);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(testUser)
                .claims(s -> s.putAll(claims))
                .build();
        JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        User updatedUser = new User()
                .userId(testUser)
                .loginUsername(testLoginUsername)
                .displayName(testUserDisplayName)
                .role(testUserRole)
                .isDisabled(false);

        when(mockUserService.updateUser(
                eq(testUser),
                eq(Optional.of(testLoginUsername)),
                eq(testUserDisplayName),
                any()))
                .thenReturn(updatedUser);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockAuthServerClientService, never()).getUserInfo(any());
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName, null);
        verify(mockAuthorizationEngine).addUser(testUser, testLoginUsername, testUserDisplayName, testUserRole, false);
    }

    @Test
    public void testDescribeCurrentUser_WithValidGroupsAndFirstLogin() throws Exception {
        ReflectionTestUtils.setField(controller, "defaultGroupsKey", "groups");
        
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", testLoginUsername);
        userInfo.put("name", testUserDisplayName);
        userInfo.put("groups", "group1,group2");
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(userInfo);

        // Mock addUserToDefaultGroups to return true for first login
        when(mockUserService.addUserToDefaultGroups(testUser, "group1,group2"))
                .thenReturn(true);
        when(mockUserService.parseAndValidateGroupIds("group1,group2"))
                .thenReturn(java.util.List.of("group1", "group2"));

        User updatedUser = new User()
                .userId(testUser)
                .loginUsername(testLoginUsername)
                .displayName(testUserDisplayName)
                .role(testUserRole)
                .isDisabled(false);
        when(mockUserService.updateUser(eq(testUser), any(), eq(testUserDisplayName), any()))
                .thenReturn(updatedUser);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockAuthServerClientService).getUserInfo(any());
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName, null);
        verify(mockAuthorizationEngine).addUser(testUser, testLoginUsername, testUserDisplayName, testUserRole, false);
        verify(mockUserService).addUserToDefaultGroups(testUser, "group1,group2");
        verify(mockUserService).parseAndValidateGroupIds("group1,group2");
        verify(mockAuthorizationEngine).addGroup("group1");
        verify(mockAuthorizationEngine).addGroup("group2");
        verify(mockAuthorizationEngine).addUserToGroup(testUser, "group1");
        verify(mockAuthorizationEngine).addUserToGroup(testUser, "group2");
    }

    @Test
    public void testDescribeCurrentUser_WithGroupsButNotFirstLogin() throws Exception {
        ReflectionTestUtils.setField(controller, "defaultGroupsKey", "groups");
        
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", testLoginUsername);
        userInfo.put("name", testUserDisplayName);
        userInfo.put("groups", "group1,group2");
        when(mockAuthServerClientService.getUserInfo(any())).thenReturn(userInfo);

        // Mock addUserToDefaultGroups to return false for not first login
        when(mockUserService.addUserToDefaultGroups(testUser, "group1,group2"))
                .thenReturn(false);

        User updatedUser = new User()
                .userId(testUser)
                .loginUsername(testLoginUsername)
                .displayName(testUserDisplayName)
                .role(testUserRole)
                .isDisabled(false);
        when(mockUserService.updateUser(eq(testUser), any(), eq(testUserDisplayName), any()))
                .thenReturn(updatedUser);

        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockAuthServerClientService).getUserInfo(any());
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName, null);
        verify(mockAuthorizationEngine).addUser(testUser, testLoginUsername, testUserDisplayName, testUserRole, false);
        verify(mockUserService).addUserToDefaultGroups(testUser, "group1,group2");
        verify(mockAuthorizationEngine, never()).addGroup(any());
        verify(mockAuthorizationEngine, never()).addUserToGroup(any(), any());
    }
}