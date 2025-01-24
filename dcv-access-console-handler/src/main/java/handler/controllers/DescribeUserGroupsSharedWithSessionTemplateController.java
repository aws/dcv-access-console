package handler.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import handler.errors.HandlerErrorMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import handler.api.DescribeUserGroupsSharedWithSessionTemplateApi;
import handler.api.DescribeUsersSharedWithSessionTemplateApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.exceptions.BadRequestException;
import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUserGroupsResponse;
import handler.model.DescribeUserGroupsSharedWithSessionTemplateRequestData;
import handler.model.DescribeUserGroupsSharedWithSessionTemplateResponse;
import handler.model.DescribeUsersRequestData;
import handler.model.DescribeUsersResponse;
import handler.model.DescribeUsersSharedWithSessionTemplateRequestData;
import handler.model.DescribeUsersSharedWithSessionTemplateResponse;
import handler.model.Error;
import handler.model.FilterToken;
import handler.model.UserGroupWithPermissions;
import handler.model.UserWithPermissions;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import handler.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.DescribeUserGroupsSharedWithSessionTemplateErrors.DESCRIBE_USER_GROUPS_SHARED_WITH_SESSION_TEMPLATE_DEFAULT_MESSAGE;
import static handler.errors.DescribeUserGroupsSharedWithSessionTemplateErrors.SESSION_TEMPLATE_ID_NULL;
import static handler.errors.DescribeUserGroupsSharedWithSessionTemplateErrors.USER_UNAUTHORIZED_ERROR;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeUserGroupsSharedWithSessionTemplateController implements
        DescribeUserGroupsSharedWithSessionTemplateApi {
    private final SessionTemplateService sessionTemplateService;

    private final UserGroupService userGroupService;

    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    private ResponseEntity<DescribeUserGroupsSharedWithSessionTemplateResponse> sendExceptionResponse(HttpStatus status, Exception e,
                                                                                                      DescribeUserGroupsSharedWithSessionTemplateRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing describeUsersSharedWithSessionTemplate for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DescribeUserGroupsSharedWithSessionTemplateResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DescribeUserGroupsSharedWithSessionTemplateResponse> describeUserGroupsSharedWithSessionTemplate(
            DescribeUserGroupsSharedWithSessionTemplateRequestData request) {
        try {
            log.info("Received describeUserGroupsSharedWithSessionTemplate request: {}", request);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!authorizationEngine.isAuthorized(PrincipalType.User, username,
                    ResourceAction.viewUserGroupsSharedWithSessionTemplate, ResourceType.SessionTemplate, request.getSessionTemplateId())) {
                String message = String.format("User %s is not authorized to view User Groups for Session Template %s", username, request.getSessionTemplateId());
                return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new BadRequestException(message), request, USER_UNAUTHORIZED_ERROR);
            }
            if (request.getSessionTemplateId() == null) {
                String message = "Session Template Id in the request cannot be null";
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException(message), request, SESSION_TEMPLATE_ID_NULL);
            }
            DescribeUserGroupsSharedWithSessionTemplateResponse response = new DescribeUserGroupsSharedWithSessionTemplateResponse();
            List<String> userGroupsIdsBySessionTemplate = sessionTemplateService.getUserGroupIdsBySessionTemplate(
                    request.getSessionTemplateId());
            log.info("User Group ids for session template id: {} are: {}", request.getSessionTemplateId(), userGroupsIdsBySessionTemplate);
            List<FilterToken> userGroupsIds = userGroupsIdsBySessionTemplate.stream()
                    .map(group -> new FilterToken().operator(FilterToken.OperatorEnum.EQUAL).value(group))
                    .toList();

            if (!userGroupsIds.isEmpty()) {
                DescribeUserGroupsResponse describeUsersResponse = userGroupService.describeUserGroups(
                        new DescribeUserGroupsRequestData().userGroupIds(userGroupsIds).nextToken(request.getNextToken()));
                log.info("DescribeUserGroupsResponse for: {} is: {}", userGroupsIds, describeUsersResponse.getUserGroups());
                response.setUserGroups(describeUsersResponse.getUserGroups().stream().map(group -> new UserGroupWithPermissions().userGroupId(group.getUserGroupId()).displayName(group.getDisplayName())).toList());
            } else {
                response.setUserGroups(List.of());
            }

            log.info("Successfully sent describeUserGroupsSharedWithSessionTemplate response of size {}", response.getUserGroups().size());
            log.debug("Full response: {}", response);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DESCRIBE_USER_GROUPS_SHARED_WITH_SESSION_TEMPLATE_DEFAULT_MESSAGE);
        }
    }
}
