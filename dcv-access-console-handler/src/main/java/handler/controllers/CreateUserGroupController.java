package handler.controllers;

import handler.api.CreateUserGroupApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.CreateUserGroupRequestData;
import handler.model.CreateUserGroupResponse;
import handler.model.Error;
import handler.model.UserGroup;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.CreateUserGroupErrors.AUTHORIZATION_ENGINE_CREATE_FAILED_ERROR;
import static handler.errors.CreateUserGroupErrors.CREATE_USER_GROUP_DEFAULT_MESSAGE;
import static handler.errors.CreateUserGroupErrors.INVALID_USER_GROUP_ID_ERROR;
import static handler.errors.CreateUserGroupErrors.NO_USER_GROUP_ID_ERROR;
import static handler.errors.CreateUserGroupErrors.USER_GROUP_ID_CONFLICT_ERROR;

@Slf4j
@RestController
@AllArgsConstructor
public class CreateUserGroupController implements CreateUserGroupApi {

    private static final String VALID_GROUP_ID_REGEX = "^[A-Za-z0-9]+$";

    private UserGroupService userGroupService;
    private SessionTemplateService sessionTemplateService;
    private AbstractAuthorizationEngine authorizationEngine;


    private ResponseEntity<CreateUserGroupResponse> sendExceptionResponse(HttpStatus status, Exception e, CreateUserGroupRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing createUserGroup for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new CreateUserGroupResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<CreateUserGroupResponse> createUserGroup(CreateUserGroupRequestData request) {
        try {
            log.info("Received createUserGroup request: {}", request);
            CreateUserGroupResponse response = new CreateUserGroupResponse();

            String userGroupId = request.getUserGroupId();
            if (StringUtils.isBlank(userGroupId)) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException(NO_USER_GROUP_ID_ERROR.getDescription()), request, NO_USER_GROUP_ID_ERROR);
            }

            if (!userGroupId.matches(VALID_GROUP_ID_REGEX)) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException(INVALID_USER_GROUP_ID_ERROR.getDescription()), request, INVALID_USER_GROUP_ID_ERROR);
            }

            UserGroup userGroup = userGroupService.createUserGroup(request.getUserGroupId(), request.getDisplayName(), false);
            if (userGroup == null) {
                String message = "Cannot create User Group with ID " + request.getUserGroupId() + " as it already exists.";
                return sendExceptionResponse(HttpStatus.CONFLICT, new BadRequestException(message), request, USER_GROUP_ID_CONFLICT_ERROR);
            }

            if (!authorizationEngine.addGroup(request.getUserGroupId())) {
                String message = "Cannot create User Group with ID " + request.getUserGroupId();
                return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, new AuthorizationServiceException(message), request, AUTHORIZATION_ENGINE_CREATE_FAILED_ERROR);
            }

            if (request.getUserIds() != null) {
                for (String userId : request.getUserIds()) {
                    if (userGroupService.addUserToGroup(userId, request.getUserGroupId()) &&
                            authorizationEngine.addUserToGroup(userId, request.getUserGroupId())) {
                        response.addSuccessfulUsersListItem(userId);
                    } else {
                        log.error("Unable to add user {} to user group {}", userId, request.getUserGroupId());
                        response.addUnsuccessfulUsersListItem(userId);
                    }
                }
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            List<String> sessionTemplateIds = getAuthorizedSessionTemplateIds(request.getSessionTemplateIds(), username, response);

            for (String sessionTemplateId : sessionTemplateIds) {
                if (authorizationEngine.addPrincipalToSharedList(PrincipalType.Group, request.getUserGroupId(),
                        ResourceType.SessionTemplate, sessionTemplateId, ShareLevel.publishedTo)) {
                    response.addSuccessfulSessionTemplateListItem(sessionTemplateId);
                } else {
                    log.error("Unable to add session template {} to user group {}", sessionTemplateId, request.getUserGroupId());
                    response.addUnsuccessfulSessionTemplateListItem(sessionTemplateId);
                }
            }

            response.setUserGroup(userGroup);
            log.info("Successfully sent createSessionTemplate response: {}", response);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, CREATE_USER_GROUP_DEFAULT_MESSAGE);
        }
    }


    public List<String> getAuthorizedSessionTemplateIds(@Nullable List<String> sessionTemplateIds, String username, CreateUserGroupResponse response) {
        List<String> authorizedSessionTemplates = new ArrayList<>();
        if (sessionTemplateIds == null) {
            return authorizedSessionTemplates;
        }
        for (String sessionTemplateId : sessionTemplateIds) {
            if (authorizationEngine.isAuthorized(PrincipalType.User, username,
                    ResourceAction.viewSessionTemplateDetails, ResourceType.SessionTemplate, sessionTemplateId)) {
                log.info("User {} is authorized to view Session Template {}", username, sessionTemplateId);
                authorizedSessionTemplates.add(sessionTemplateId);
            } else {
                log.warn("User {} is not authorized to view Session Template {}", username, sessionTemplateId);
                response.addUnsuccessfulSessionTemplateListItem(sessionTemplateId);
            }
        }
        return authorizedSessionTemplates;
    }
}
