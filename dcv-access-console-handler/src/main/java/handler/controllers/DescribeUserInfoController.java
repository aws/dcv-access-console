package handler.controllers;

import handler.api.DescribeUserInfoApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.model.DescribeUserInfoResponse;
import handler.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@Slf4j
public class DescribeUserInfoController implements DescribeUserInfoApi {
    private AbstractAuthorizationEngine authorizationEngine;
    private UserService userService;

    @Override
    public ResponseEntity<DescribeUserInfoResponse> describeUserInfo() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
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
}