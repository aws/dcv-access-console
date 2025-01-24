package handler.controllers;

import handler.api.DescribeUsersSharedWithSessionTemplateApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.DescribeUsersRequestData;
import handler.model.DescribeUsersResponse;
import handler.model.DescribeUsersSharedWithSessionTemplateRequestData;
import handler.model.DescribeUsersSharedWithSessionTemplateResponse;
import handler.model.Error;
import handler.model.FilterToken;
import handler.model.UserWithPermissions;
import handler.services.SessionTemplateService;
import handler.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.DescribeUserSharedWithSessionTemplateErrors.DESCRIBE_USER_SHARED_WITH_SESSION_TEMPLATE_DEFAULT_MESSAGE;
import static handler.errors.DescribeUserSharedWithSessionTemplateErrors.SESSION_TEMPLATE_ID_NULL;
import static handler.errors.DescribeUserSharedWithSessionTemplateErrors.USER_UNAUTHORIZED_ERROR;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeUsersSharedWithSessionTemplateController implements DescribeUsersSharedWithSessionTemplateApi {
    private final SessionTemplateService sessionTemplateService;

    private final UserService userService;

    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    private ResponseEntity<DescribeUsersSharedWithSessionTemplateResponse> sendExceptionResponse(HttpStatus status, Exception e,
                                                                                                 DescribeUsersSharedWithSessionTemplateRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing describeUsersSharedWithSessionTemplate for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DescribeUsersSharedWithSessionTemplateResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DescribeUsersSharedWithSessionTemplateResponse> describeUsersSharedWithSessionTemplate(
            DescribeUsersSharedWithSessionTemplateRequestData request) {
        try {
            log.info("Received describeUsersSharedWithSessionTemplate request: {}", request);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!authorizationEngine.isAuthorized(PrincipalType.User, username,
                    ResourceAction.viewUsersSharedWithSessionTemplate, ResourceType.SessionTemplate, request.getSessionTemplateId())) {
                log.warn("User {} is not authorized to view Users for Session Template {}", username, request.getSessionTemplateId());
                String message = String.format("User %s is not authorized to view Users for Session Template %s", username, request.getSessionTemplateId());
                return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new AuthorizationServiceException(message), request, USER_UNAUTHORIZED_ERROR);
            }
            if (request.getSessionTemplateId() == null) {
                String message = "The Session Template Id in the request cannot be null";
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException(message), request, SESSION_TEMPLATE_ID_NULL);
            }
            DescribeUsersSharedWithSessionTemplateResponse response = new DescribeUsersSharedWithSessionTemplateResponse();
            List<String> userIdsBySessionTemplate = sessionTemplateService.getUserIdsBySessionTemplate(
                    request.getSessionTemplateId());
            log.info("User ids for session template id: {} are: {}", request.getSessionTemplateId(), userIdsBySessionTemplate);
            List<FilterToken> userIds = userIdsBySessionTemplate.stream()
                    .map(user -> new FilterToken().operator(FilterToken.OperatorEnum.EQUAL).value(user))
                    .collect(Collectors.toList());
            if (!userIds.isEmpty()) {
                DescribeUsersResponse describeUsersResponse = userService.describeUsers(
                        new DescribeUsersRequestData().userIds(userIds).nextToken(request.getNextToken()));
                log.info("DescribeUsersResponse for: {} is: {}", userIds, describeUsersResponse.getUsers());
                response.setUsers(describeUsersResponse.getUsers().stream().map(user -> new UserWithPermissions().userId(user.getUserId()).displayName(user.getDisplayName())).toList());
            } else {
                response.setUsers(List.of());
            }

            log.info("Successfully sent describeUsersSharedWithSessionTemplate response of size {}", response.getUsers().size());
            log.debug("Full response: {}", response);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DESCRIBE_USER_SHARED_WITH_SESSION_TEMPLATE_DEFAULT_MESSAGE);
        }
    }
}
