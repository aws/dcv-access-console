// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.CreateSessionsApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.SystemAction;
import handler.brokerclients.BrokerClient;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.model.CreateSessionUIRequestData;
import handler.model.CreateSessionsUIResponse;
import handler.model.DescribeSessionTemplatesResponse;
import handler.model.Error;
import handler.model.SessionTemplate;
import handler.model.UnsuccessfulCreateSessionUIRequestData;
import handler.model.FilterToken;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.CreateSessionRequestData;
import handler.model.DeleteSessionUIRequestData;
import handler.model.SessionWithPermissions;
import handler.services.SessionTemplateService;
import handler.utils.Filter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.CommonErrorsEnum.BROKER_AUTHENTICATION_ERROR;
import static handler.errors.CreateSessionErrors.CREATE_SESSION_DEFAULT_MESSAGE;
import static handler.errors.CreateSessionErrors.USER_UNAUTHORIZED_SESSION_CREATION_FOR_OTHERS;

@AllArgsConstructor
@RestController
@Slf4j
public class CreateSessionsController implements CreateSessionsApi {
    private BrokerClient brokerClient;
    private SessionTemplateService sessionTemplateService;
    private final AbstractAuthorizationEngine authorizationEngine;

    private final Filter<DescribeSessionTemplatesRequestData, SessionTemplate> sessionTemplateFilter;

    private ResponseEntity<CreateSessionsUIResponse> sendExceptionResponse(HttpStatus status, Exception e, List<CreateSessionUIRequestData> requests, HandlerErrorMessage errorMessage) {
        log.error("Error while performing createSessions for {}", requests, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new CreateSessionsUIResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<CreateSessionsUIResponse> createSessions(List<CreateSessionUIRequestData> requests) {
        try {
            log.info("Received createSessions request: {}", requests);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Pair<CreateSessionUIRequestData, SessionTemplate>> brokerRequests = new ArrayList<>();
            List<UnsuccessfulCreateSessionUIRequestData> unsuccessfulRequests = new ArrayList<>();
            for(CreateSessionUIRequestData request: requests) {
                if (StringUtils.isNotBlank(request.getOwner()) && !request.getOwner().equals(authorizationEngine.getUserLoginUsername(username))) {
                    // Owner is not empty, and is not the current user
                    if (!authorizationEngine.isAuthorized(PrincipalType.User, username, SystemAction.createSessionsForOthers)) {
                        String message = String.format("User %s is not authorized to create sessions for other users. ", username);
                        return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new AuthorizationServiceException(message), requests, USER_UNAUTHORIZED_SESSION_CREATION_FOR_OTHERS);
                    }
                    log.info("User {} was authorized to create session for others...", username);
                } else {
                    // We should be creating this session for the logged-in user
                    log.info("Setting the Session Owner to the current user: {}", username);
                    request.setOwner(authorizationEngine.getUserLoginUsername(username));
                }

                FilterToken filterToken = new FilterToken()
                        .operator(FilterToken.OperatorEnum.EQUAL)
                        .value(request.getSessionTemplateId());
                DescribeSessionTemplatesRequestData describeSessionTemplatesRequestData = new DescribeSessionTemplatesRequestData().addIdsItem(filterToken);
                SessionTemplate sessionTemplate;
                DescribeSessionTemplatesResponse response;
                // Loop until we either find the session template or run out of pages to check.
                do {
                    response = sessionTemplateService.describeSessionTemplates(describeSessionTemplatesRequestData);

                    // Why are the filters in the controller when they should be in the service? It doesn't make sense
                    List<SessionTemplate> filteredSessionTemplates = sessionTemplateFilter.getFiltered(describeSessionTemplatesRequestData, response.getSessionTemplates());
                    sessionTemplate = !filteredSessionTemplates.isEmpty() ? filteredSessionTemplates.get(0) : null;
                    describeSessionTemplatesRequestData.nextToken(response.getNextToken());
                } while (sessionTemplate == null && response.getNextToken() != null);

                if(sessionTemplate != null) {
                    if(!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.useSpecificSessionTemplate, ResourceType.SessionTemplate, sessionTemplate.getId())) {
                        HashMap<String, String> errors = new HashMap<>();
                        errors.put("Session Template", String.format("User %s is not authorized to use session template %s", username, sessionTemplate.getId()));
                        UnsuccessfulCreateSessionUIRequestData unsuccessfulCreateSessionRequestData = new UnsuccessfulCreateSessionUIRequestData().failureReasons(errors);
                        handler.model.CreateSessionRequestData createSessionRequestData = new CreateSessionRequestData().name(request.getName()).owner(request.getOwner());
                        unsuccessfulCreateSessionRequestData.setCreateSessionRequestData(createSessionRequestData);
                        unsuccessfulRequests.add(unsuccessfulCreateSessionRequestData);
                        continue;
                    }
                    Pair<CreateSessionUIRequestData, SessionTemplate> brokerRequest = new Pair<>(request, sessionTemplate);
                    brokerRequests.add(brokerRequest);
                } else {
                    HashMap<String, String> errors = new HashMap<>();
                    errors.put("Session Template", String.format("Session template %s not found", request.getSessionTemplateId()));
                    UnsuccessfulCreateSessionUIRequestData unsuccessfulCreateSessionRequestData = new UnsuccessfulCreateSessionUIRequestData().failureReasons(errors);
                    handler.model.CreateSessionRequestData createSessionRequestData = new CreateSessionRequestData().name(request.getName()).owner(request.getOwner());
                    unsuccessfulCreateSessionRequestData.setCreateSessionRequestData(createSessionRequestData);
                    unsuccessfulRequests.add(unsuccessfulCreateSessionRequestData);
                }
            }

            CreateSessionsUIResponse response = new CreateSessionsUIResponse();
            if(!brokerRequests.isEmpty()) {
                response = brokerClient.createSessions(brokerRequests);

                List<DeleteSessionUIRequestData> deleteRequests = new ArrayList<>();
                if (response.getSuccessfulList() != null) {
                    for(SessionWithPermissions session: response.getSuccessfulList()) {
                        if (!authorizationEngine.addSession(session.getId(), session.getOwner())) {
                            deleteRequests.add(new DeleteSessionUIRequestData().sessionId(session.getId()).owner(session.getOwner()));
                        }
                    }
                }
                if (!deleteRequests.isEmpty()) {
                    brokerClient.deleteSessions(deleteRequests);
                }
            }
            unsuccessfulRequests.forEach(response::addUnsuccessfulListItem);

            log.info("Successfully sent createSessions response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, requests, BAD_REQUEST_ERROR);
        } catch (BrokerAuthenticationException e) {
            return sendExceptionResponse(HttpStatus.UNAUTHORIZED, e, requests, BROKER_AUTHENTICATION_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, requests, CREATE_SESSION_DEFAULT_MESSAGE);
        }
    }
}
