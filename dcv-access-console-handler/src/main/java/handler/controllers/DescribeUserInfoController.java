// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.DescribeUserInfoApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.model.DescribeUserInfoResponse;
import handler.model.User;
import handler.services.AuthServerClientService;
import handler.services.UserService;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Slf4j
public class DescribeUserInfoController implements DescribeUserInfoApi {
    private final AbstractAuthorizationEngine authorizationEngine;
    private final UserService userService;

    @Autowired(required = false)
    private AuthServerClientService authServerClientService;

    @Value("${jwt-login-username-claim-key:#{null}}")
    private String loginUsernameKey;

    @Value("${jwt-display-name-claim-key:#{null}}")
    private String displayNameKey;

    @Value("${auth-server-claims-from-access-token:false}")
    private boolean useAuthServerClaimsFromAccessToken;

    @Override
    public ResponseEntity<DescribeUserInfoResponse> describeUserInfo() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if ((StringUtils.isNotEmpty(loginUsernameKey) || StringUtils.isNotEmpty(displayNameKey))
                    && authServerClientService != null) {
                updateUserFromAuthServer(username);
            }

            String displayName = authorizationEngine.getUserDisplayName(username);
            String role = authorizationEngine.getUserRole(username);

            userService.updateLastLoggedInTime(username);

            DescribeUserInfoResponse describeCurrentUserResponse = new DescribeUserInfoResponse()
                    .id(username)
                    .displayName(displayName)
                    .role(role);
            return new ResponseEntity<>(describeCurrentUserResponse, HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void updateUserFromAuthServer(String username) {
        Map<String, Object> userInfo = null;

        if (useAuthServerClaimsFromAccessToken) {
            userInfo = ((JwtAuthenticationToken) SecurityContextHolder.getContext()
                    .getAuthentication()).getToken().getClaims();
        } else {
            String accessToken = ((JwtAuthenticationToken) SecurityContextHolder.getContext()
                    .getAuthentication()).getToken().getTokenValue();

            userInfo = authServerClientService.getUserInfo(accessToken);
        }
        if (userInfo == null) {
            return;
        }

        Optional<String> loginUsername = null;
        String displayName = null;

        if (!StringUtils.isEmpty(loginUsernameKey)) {
            if (userInfo.containsKey(loginUsernameKey)) {
                loginUsername = Optional.ofNullable(String.valueOf(userInfo.get(loginUsernameKey)));
            } else {
                log.warn("Claim {} not found in userInfo response", loginUsernameKey);
            }
        }

        if (!StringUtils.isEmpty(displayNameKey)) {
            if (userInfo.containsKey(displayNameKey)) {
                displayName = String.valueOf(userInfo.get(displayNameKey));
            } else {
                log.warn("Claim {} not found in userInfo response", displayNameKey);
            }
        }

        try {
            User updatedUser = userService.updateUser(username, loginUsername, displayName);
            if (updatedUser != null) {
                authorizationEngine.addUser(username, updatedUser.getLoginUsername(), updatedUser.getDisplayName(), updatedUser.getRole(), updatedUser.getIsDisabled());
            }
        } catch (Exception e) {
            log.error("Error updating the user using the claims retrieved", e);
        }
    }
}