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
import handler.model.DescribeUsersRequestData;
import handler.model.Error;
import handler.model.FilterToken;
import handler.model.FilterTokenStrict;
import handler.model.SessionTemplate;
import handler.model.User;
import handler.services.SessionTemplateService;
import handler.services.UserService;
import handler.utils.Filter;
import handler.utils.NextToken;
import handler.utils.Sort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.DescribeSessionTemplatesErrors.DESCRIBE_SESSION_TEMPLATES_DEFAULT_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeSessionTemplatesController implements DescribeSessionTemplatesApi {
    private final SessionTemplateService sessionTemplateService;
    private final UserService userService;
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

            LoginUsernameResolution resolution = resolveLoginUsernameFilters(request);

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

                filteredSessionTemplates = preFilterExclusions(filteredSessionTemplates, resolution);

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

    private record LoginUsernameResolution(
        Set<String> excludedCreatedByUserIds,
        Set<String> excludedLastModifiedByUserIds
    ) {}

    private List<SessionTemplate> preFilterExclusions(List<SessionTemplate> templates, LoginUsernameResolution resolution) {
        return templates.stream()
            .filter(t -> !resolution.excludedCreatedByUserIds.contains(t.getCreatedBy()))
            .filter(t -> !resolution.excludedLastModifiedByUserIds.contains(t.getLastModifiedBy()))
            .toList();
    }

    private LoginUsernameResolution resolveLoginUsernameFilters(DescribeSessionTemplatesRequestData request) {
        Set<String> excludedCreatedBy = new HashSet<>();
        Set<String> excludedLastModifiedBy = new HashSet<>();

        if (!CollectionUtils.isEmpty(request.getCreatedByLoginUsername())) {
            ResolvedFilters resolved = resolveLoginUsernameFiltersToUserIdFilters(request.getCreatedByLoginUsername());
            request.setCreatedBy(resolved.filters);
            excludedCreatedBy.addAll(resolved.excludedUserIds);
        }

        if (!CollectionUtils.isEmpty(request.getLastModifiedByLoginUsername())) {
            ResolvedFilters resolved = resolveLoginUsernameFiltersToUserIdFilters(request.getLastModifiedByLoginUsername());
            request.setLastModifiedBy(resolved.filters);
            excludedLastModifiedBy.addAll(resolved.excludedUserIds);
        }

        if (!CollectionUtils.isEmpty(request.getUsersSharedWithLoginUsername())) {
            request.setUsersSharedWith(resolveLoginUsernameFiltersForUsersSharedWith(request.getUsersSharedWithLoginUsername()));
        }

        return new LoginUsernameResolution(excludedCreatedBy, excludedLastModifiedBy);
    }

    private List<FilterTokenStrict> resolveLoginUsernameFiltersForUsersSharedWith(List<FilterToken> loginUsernameFilters) {
        DescribeUsersRequestData userRequest = new DescribeUsersRequestData();
        userRequest.setLoginUsernames(loginUsernameFilters.stream()
            .map(f -> new FilterToken().operator(FilterToken.OperatorEnum.EQUAL).value(f.getValue()))
            .toList());

        List<User> users = userService.describeUsers(userRequest).getUsers();
        Map<String, String> loginToUserId = users.stream()
            .collect(Collectors.toMap(
                u -> u.getLoginUsername() != null ? u.getLoginUsername() : u.getUserId(),
                User::getUserId,
                (a, b) -> a));

        List<FilterTokenStrict> result = new ArrayList<>();
        for (FilterToken filter : loginUsernameFilters) {
            String userId = loginToUserId.get(filter.getValue());
            FilterTokenStrict.OperatorEnum resultOp = filter.getOperator() == FilterToken.OperatorEnum.NOT_EQUAL
                ? FilterTokenStrict.OperatorEnum.NOT_EQUAL
                : FilterTokenStrict.OperatorEnum.EQUAL;
            result.add(new FilterTokenStrict().operator(resultOp).value(userId != null ? userId : filter.getValue()));
        }
        return result;
    }

    private record ResolvedFilters(List<FilterToken> filters, Set<String> excludedUserIds) {}

    private ResolvedFilters resolveLoginUsernameFiltersToUserIdFilters(List<FilterToken> loginUsernameFilters) {
        List<FilterToken> filters = new ArrayList<>();
        Set<String> excludedUserIds = new HashSet<>();

        // Group filters by lookup operator type to batch DB calls
        Map<FilterToken.OperatorEnum, List<FilterToken>> filtersByLookupOp = new HashMap<>();
        filtersByLookupOp.put(FilterToken.OperatorEnum.EQUAL, new ArrayList<>());
        filtersByLookupOp.put(FilterToken.OperatorEnum.CONTAINS, new ArrayList<>());

        for (FilterToken filter : loginUsernameFilters) {
            FilterToken.OperatorEnum op = filter.getOperator();
            FilterToken.OperatorEnum lookupOp = (op == FilterToken.OperatorEnum.CONTAINS || op == FilterToken.OperatorEnum.NOT_CONTAINS)
                ? FilterToken.OperatorEnum.CONTAINS : FilterToken.OperatorEnum.EQUAL;
            filtersByLookupOp.get(lookupOp).add(filter);
        }


        // Batch lookup for each operator type
        for (var entry : filtersByLookupOp.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            FilterToken.OperatorEnum lookupOp = entry.getKey();
            List<FilterToken> filtersForOp = entry.getValue();

            DescribeUsersRequestData userRequest = new DescribeUsersRequestData();
            userRequest.setLoginUsernames(filtersForOp.stream()
                .map(f -> new FilterToken().operator(lookupOp).value(f.getValue()))
                .toList());

            List<User> users = userService.describeUsers(userRequest).getUsers();

            for (FilterToken filter : filtersForOp) {
                FilterToken.OperatorEnum op = filter.getOperator();
                String filterVal = filter.getValue();
                
                List<User> matchingUsers = users.stream().filter(u -> {
                    String displayName = u.getLoginUsername() != null ? u.getLoginUsername() : u.getUserId();
                    if (displayName == null) return false;
                    return lookupOp == FilterToken.OperatorEnum.EQUAL
                        ? displayName.equals(filterVal)
                        : displayName.contains(filterVal);
                }).toList();

                if (matchingUsers.isEmpty()) {
                    if (op == FilterToken.OperatorEnum.EQUAL || op == FilterToken.OperatorEnum.CONTAINS) {
                        filters.add(filter);
                    }
                } else if (op == FilterToken.OperatorEnum.NOT_CONTAINS) {
                    matchingUsers.forEach(u -> excludedUserIds.add(u.getUserId()));
                } else {
                    FilterToken.OperatorEnum resultOp = (op == FilterToken.OperatorEnum.CONTAINS)
                        ? FilterToken.OperatorEnum.EQUAL : op;
                    matchingUsers.forEach(u -> 
                        filters.add(new FilterToken().operator(resultOp).value(u.getUserId())));
                }
            }
        }
        return new ResolvedFilters(filters, excludedUserIds);
    }
}