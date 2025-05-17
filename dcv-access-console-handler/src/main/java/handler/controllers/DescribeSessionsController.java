// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.SystemAction;
import handler.errors.HandlerErrorMessage;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.DescribeSessionsUIResponse;
import handler.model.FilterToken;
import handler.model.FilterTokenStrict;
import handler.model.Session;
import handler.model.SessionWithPermissions;
import handler.model.Error;
import handler.api.DescribeSessionsApi;
import handler.brokerclients.BrokerClient;
import handler.utils.Filter;
import handler.utils.Sort;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BadRequestException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.CommonErrorsEnum.BROKER_AUTHENTICATION_ERROR;
import static handler.errors.DescribeSessionsErrors.DESCRIBE_SESSIONS_DEFAULT_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeSessionsController implements DescribeSessionsApi {
    private final BrokerClient brokerClient;
    private final Filter<DescribeSessionsUIRequestData, SessionWithPermissions> sessionFilter;
    private final Sort<DescribeSessionsUIRequestData, SessionWithPermissions> sessionSort;
    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    private ResponseEntity<DescribeSessionsUIResponse> sendExceptionResponse(HttpStatus status, Exception e, DescribeSessionsUIRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing describeSessions for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DescribeSessionsUIResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DescribeSessionsUIResponse> describeSessions(DescribeSessionsUIRequestData request) {
        try {
            log.info("Received describeSessions request: {}", request);

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            int resultsRemaining = request.getMaxResults() != null ? request.getMaxResults() : defaultMaxResults;
            request.setMaxResults(resultsRemaining);
            List<SessionWithPermissions> sessions = new ArrayList<>();
            DescribeSessionsUIResponse response;

            do {
                response = brokerClient.describeSessions(request);

                List<SessionWithPermissions> newSessions = sessionFilter.getFiltered(request, response.getSessions());

                if (request.getUserId() != null && authorizationEngine
                        .isAuthorized(PrincipalType.User, username, SystemAction.describeSessionsForOthers)) {
                    newSessions = getAuthorizedSessions(newSessions, request.getUserId());
                } else {
                    log.debug("Not filtering by UserId specified because it is either null, or the requesting user is not authorized...");
                    newSessions = getAuthorizedSessions(newSessions, username);
                }
                sessions.addAll(newSessions);

                resultsRemaining -= newSessions.size();
                request.setMaxResults(resultsRemaining);
                request.setNextToken(response.getNextToken());
            } while (resultsRemaining > 0 && response.getNextToken() != null);

            sessions = sessionSort.getSorted(request, sessions);
            response.setSessions(sessions);

            log.info("Successfully sent describeSessions response of size {}", response.getSessions().size());
            log.debug("Full response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (BrokerAuthenticationException | UsernameNotFoundException e) {
            return sendExceptionResponse(HttpStatus.UNAUTHORIZED, e, request, BROKER_AUTHENTICATION_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DESCRIBE_SESSIONS_DEFAULT_MESSAGE);
        }
    }

    private List<SessionWithPermissions> getAuthorizedSessions(List<SessionWithPermissions> sessions, String username) {
        List<SessionWithPermissions> authorizedSessions = new ArrayList<>();
        for (SessionWithPermissions session : sessions) {
            if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.viewSessionDetails, ResourceType.Session, session.getId())) {
                log.warn("User {} not authorized to view session {}", username, session.getId());
            } else {
                log.info("User {} is authorized to view session {}", username, session.getId());
                session.levelOfAccess(session.getOwner().equals(authorizationEngine.getUserLoginUsername(username)) ? "Owner" : "Admin");
                authorizedSessions.add(session);
            }
        }
        return authorizedSessions;
    }
}
