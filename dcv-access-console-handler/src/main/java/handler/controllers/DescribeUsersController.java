// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.DescribeUsersApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.DescribeUsersRequestData;
import handler.model.DescribeUsersResponse;
import handler.model.Error;
import handler.model.User;
import handler.persistence.UserEntity;
import handler.services.UserService;
import handler.utils.NextToken;
import handler.utils.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.DescribeUsersErrors.DESCRIBE_USERS_DEFAULT_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeUsersController implements DescribeUsersApi {
    private final UserService userService;
    private final Sort<DescribeUsersRequestData, User> userSort;
    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    private ResponseEntity<DescribeUsersResponse> sendExceptionResponse(HttpStatus status, Exception e, DescribeUsersRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing describeUsers for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DescribeUsersResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DescribeUsersResponse> describeUsers(DescribeUsersRequestData request) {
        try {
            log.info("Received describeUsers request: {}", request);

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<User> users = new ArrayList<>();
            DescribeUsersResponse response;
            int resultsRemaining = request.getMaxResults() != null ? request.getMaxResults() : defaultMaxResults;
            request.setMaxResults(resultsRemaining);

            do {
                response = userService.describeUsers(request);

                List<User> newUsers = getAuthorizedUsers(response.getUsers(), username);

                if (newUsers.size() > resultsRemaining) {
                    users.addAll(newUsers.subList(0, resultsRemaining));

                    NextToken nextToken = NextToken.deserialize(request.getNextToken(), UserEntity.class);
                    nextToken.setPageOffset(OptionalInt.of(resultsRemaining));
                    response.setNextToken(NextToken.serialize(nextToken, UserEntity.class));
                } else {
                    users.addAll(newUsers);
                    request.setNextToken(response.getNextToken());
                }

                resultsRemaining -= newUsers.size();
            } while (resultsRemaining > 0 && response.getNextToken() != null);

            users = userSort.getSorted(request, users);
            response.setUsers(users);

            log.info("Successfully sent describeUsers response of size {}", response.getUsers().size());
            log.debug("Full response: {}", response);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DESCRIBE_USERS_DEFAULT_MESSAGE);
        }
    }

    private List<User> getAuthorizedUsers(List<User> users, String username) {
        List<User> authorizedUsers = new ArrayList<>();
        for (User user : users) {
            if (authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.viewUserDetails, ResourceType.User, user.getUserId())) {
                log.info("User {} is authorized to view User {}", username, user.getUserId());
                authorizedUsers.add(user);
            } else {
                log.warn("User {} is not authorized to view User {}", username, user.getUserId());
            }
        }
        return authorizedUsers;
    }
}
