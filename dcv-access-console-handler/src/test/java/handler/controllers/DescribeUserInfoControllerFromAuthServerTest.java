// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.services.AuthServerClientService;
import handler.services.UserService;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    }

    @Test
    public void testDescribeCurrentUser_WithAuthServerUpdate() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", testLoginUsername);
        userInfo.put("name", testUserDisplayName);
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
        verify(mockAuthorizationEngine).updateUser(testUser, testLoginUsername, testUserDisplayName);
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
        verify(mockAuthorizationEngine, times(0)).updateUser(any(), any(), any());
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
        verify(mockAuthorizationEngine, times(0)).updateUser(any(), any(), any());
    }
}