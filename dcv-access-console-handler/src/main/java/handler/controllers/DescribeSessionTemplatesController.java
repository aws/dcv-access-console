// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.DescribeSessionTemplatesApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.SystemAction;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeSessionTemplatesResponse;
import handler.model.Error;
import handler.model.FilterToken;
import handler.model.FilterTokenStrict;
import handler.model.SessionTemplate;
import handler.services.SessionTemplateService;
import handler.utils.Filter;
import handler.utils.NextToken;
import handler.utils.Sort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.util.StringUtil;
import org.mariadb.jdbc.util.StringUtils;
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
import static handler.errors.DescribeSessionTemplatesErrors.DESCRIBE_SESSION_TEMPLATES_DEFAULT_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeSessionTemplatesController implements DescribeSessionTemplatesApi {
    private final SessionTemplateService sessionTemplateService;
    private final Filter<DescribeSessionTemplatesRequestData, SessionTemplate> sessionTemplateFilter;
    private final Sort<DescribeSessionTemplatesRequestData, SessionTemplate> sessionTemplateSort;
    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    private ResponseEntity<DescribeSessionTemplatesResponse> sendExceptionResponse(HttpStatus status, Exception e,
                                                                                   DescribeSessionTemplatesRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing describeSessionTemplates for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DescribeSessionTemplatesResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DescribeSessionTemplatesResponse> describeSessionTemplates(
            DescribeSessionTemplatesRequestData request) {
        try {
            log.info("Received describeSessionTemplates request: {}", request);

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            List<SessionTemplate> sessionTemplates = new ArrayList<>();
            DescribeSessionTemplatesResponse response;
            int resultsRemaining = request.getMaxResults() != null ? request.getMaxResults() : defaultMaxResults;
            request.setMaxResults(resultsRemaining);

            do {
                response = sessionTemplateService.describeSessionTemplates(request);

                List<SessionTemplate> filteredSessionTemplates = response.getSessionTemplates();

                if (((request.getUsersSharedWith() != null && !request.getUsersSharedWith().isEmpty()) ||
                        (request.getGroupsSharedWith() != null && !request.getGroupsSharedWith().isEmpty()))) {
                    if (authorizationEngine.isAuthorized(PrincipalType.User, username, SystemAction.describeSessionTemplatesForOthers)) {
                        filteredSessionTemplates = sessionTemplateService.filterByGroupId(request, filteredSessionTemplates);
                        filteredSessionTemplates = sessionTemplateService.filterByUserId(request, filteredSessionTemplates);
                    } else {
                        log.warn("User {} is not authorized to perform describeSessionTemplates for others", username);
                    }
                }

                filteredSessionTemplates = sessionTemplateFilter.getFiltered(request, filteredSessionTemplates);

                if (request.getUserId() != null && authorizationEngine.isAuthorized(PrincipalType.User, username, SystemAction.describeSessionTemplatesForOthers)) {
                    filteredSessionTemplates = getAuthorizedSessionTemplates(filteredSessionTemplates, request.getUserId());
                } else {
                    log.debug("Not filtering by UserId specified because it is either null, or the requesting user is not authorized...");
                    filteredSessionTemplates = getAuthorizedSessionTemplates(filteredSessionTemplates, username);
                }

                if (filteredSessionTemplates.size() > resultsRemaining) {
                    sessionTemplates.addAll(filteredSessionTemplates.subList(0, resultsRemaining));

                    NextToken nextToken = NextToken.deserialize(request.getNextToken(), SessionTemplate.class);
                    nextToken.setPageOffset(OptionalInt.of(resultsRemaining));
                    response.setNextToken(NextToken.serialize(nextToken, SessionTemplate.class));
                } else {
                    sessionTemplates.addAll(filteredSessionTemplates);
                    request.setNextToken(response.getNextToken());
                }

                resultsRemaining -= filteredSessionTemplates.size();
            } while (resultsRemaining > 0 && response.getNextToken() != null);

            sessionTemplates = sessionTemplateSort.getSorted(request, sessionTemplates);
            response.setSessionTemplates(sessionTemplates);
            log.info("Successfully sent describeSessionTemplates response of size {}", response.getSessionTemplates().size());
            log.debug("Full response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DESCRIBE_SESSION_TEMPLATES_DEFAULT_MESSAGE);
        }
    }

    private List<SessionTemplate> getAuthorizedSessionTemplates(List<SessionTemplate> sessionTemplates, String username) {
        List<SessionTemplate> authorizedSessionTemplates = new ArrayList<>();
        for (SessionTemplate sessionTemplate : sessionTemplates) {
            if (authorizationEngine.isAuthorized(PrincipalType.User, username,
                    ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, sessionTemplate.getId())) {
                log.info("User {} is authorized to view Session Template {}", username, sessionTemplate.getId());
                authorizedSessionTemplates.add(sessionTemplate);
            } else {
                log.warn("User {} is not authorized to view Session Template {}", username, sessionTemplate.getId());
            }
        }
        return authorizedSessionTemplates;
    }
}