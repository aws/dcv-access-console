// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.DescribeUserInfoApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.model.DescribeUserInfoResponse;
import handler.services.AuthServerClientService;
import handler.services.UserService;
import java.util.Map;
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

    @Override
    public ResponseEntity<DescribeUserInfoResponse> describeUserInfo() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String displayName = authorizationEngine.getUserDisplayName(username);
            String role = authorizationEngine.getUserRole(username);

            if ((StringUtils.isNotEmpty(loginUsernameKey) || StringUtils.isNotEmpty(displayNameKey))
                    && authServerClientService != null) {
                updateUserFromAuthServer(username);
            }

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
        String accessToken = ((JwtAuthenticationToken) SecurityContextHolder.getContext()
                .getAuthentication()).getToken().getTokenValue();

        Map<String, Object> userInfo = authServerClientService.getUserInfo(accessToken);
        if (userInfo != null) {
            String loginUsername = null;
            String displayName = null;

            if (!StringUtils.isEmpty(loginUsernameKey)) {
                if (userInfo.containsKey(loginUsernameKey)) {
                    loginUsername = userInfo.get(loginUsernameKey).toString();
                } else {
                    log.warn("Claim {} not found in userInfo response", loginUsernameKey);
                    return;
                }
            }

            if (!StringUtils.isEmpty(displayNameKey)) {
                if (userInfo.containsKey(displayNameKey)) {
                    displayName = userInfo.get(displayNameKey).toString();
                } else {
                    log.warn("Claim {} not found in userInfo response", displayNameKey);
                    return;
                }
            }

            try {
                authorizationEngine.updateUser(username, loginUsername, displayName);
            } catch (Exception e) {
                log.error("Error updating the user using the claims retrieved", e);
            }
        }
    }
}