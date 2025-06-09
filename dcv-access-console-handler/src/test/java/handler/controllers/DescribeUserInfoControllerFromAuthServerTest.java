// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.services.AuthServerClientService;
import handler.model.User;
import handler.services.UserService;
import java.time.Instant;
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
        "jwt-display-name-claim-key=name"
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
        when(mockUserService.updateUser(eq(testUser), any(), eq(testUserDisplayName)))
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
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName);
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
        verify(mockUserService).updateUser(any(), any(), any());
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
        verify(mockUserService, never()).updateUser(any(), any(), any());
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

        when(mockUserService.updateUser(any(), any(), any()))
                .thenThrow(new RuntimeException("Update failed"));

        mvc.perform(get(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                        .header(HttpHeaders.ORIGIN, origin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));

        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName);
        verify(mockAuthorizationEngine, never()).addUser(any(), any(), any(), any(), anyBoolean());
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
                eq(testUserDisplayName)))
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
        verify(mockUserService).updateUser(testUser, Optional.of(testLoginUsername), testUserDisplayName);
        verify(mockAuthorizationEngine).addUser(testUser, testLoginUsername, testUserDisplayName, testUserRole, false);
    }
}