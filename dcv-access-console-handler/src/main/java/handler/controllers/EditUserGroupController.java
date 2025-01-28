// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.EditUserGroupApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.EditUserGroupRequestData;
import handler.model.EditUserGroupResponse;
import handler.model.Error;
import handler.model.UserGroup;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.EditUserGroupErrors.EDIT_USER_GROUP_DEFAULT_MESSAGE;
import static handler.errors.EditUserGroupErrors.MISSING_USER_GROUP_ID;
import static handler.errors.EditUserGroupErrors.USER_NOT_AUTHORIZED_TO_EDIT_USER_GROUP;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EditUserGroupController implements EditUserGroupApi {
    private final AbstractAuthorizationEngine authorizationEngine;
    private final UserGroupService userGroupService;
    private final SessionTemplateService sessionTemplateService;

    private ResponseEntity<EditUserGroupResponse> sendExceptionResponse(HttpStatus status, Exception e, EditUserGroupRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing editUserGroup for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new EditUserGroupResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<EditUserGroupResponse> editUserGroup(EditUserGroupRequestData request) {
        try {
            log.info("Received editUserGroup request: {}", request);

            String userGroupId = request.getUserGroupId();
            if (StringUtils.isBlank(userGroupId)) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException("UserGroupId is required"), request, MISSING_USER_GROUP_ID);
            }
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.editUserGroup, ResourceType.Group, userGroupId)) {
                String message = String.format("User %s is not authorized to edit user group %s.", username, userGroupId);
                return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new BadRequestException(message), request, USER_NOT_AUTHORIZED_TO_EDIT_USER_GROUP);
            }

            UserGroup editedUserGroup = userGroupService.editUserGroup(request);

            // Update the authorization engine with the new members
            if (request.getUserIdsToRemove() != null) {
                for (String userId : request.getUserIdsToRemove()) {
                    authorizationEngine.removeUserFromGroup(userId, userGroupId);
                }
                log.info("Removed {} users from the group {} in the authorization engine", request.getUserIdsToRemove().size(), userGroupId);
            }

            if (request.getUserIdsToAdd() != null) {
                for (String userId : request.getUserIdsToAdd()) {
                    authorizationEngine.addUserToGroup(userId, userGroupId);
                }
                log.info("Added {} users to group {} in the authorization engine", request.getUserIdsToAdd().size(), userGroupId);
            }

            if (request.getSessionTemplateIdsToRemove() != null) {
                for (String sessionTemplateId : request.getSessionTemplateIdsToRemove()) {
                    authorizationEngine.removePrincipalFromSharedList(PrincipalType.Group, userGroupId, ResourceType.SessionTemplate, sessionTemplateId, ShareLevel.publishedTo);
                }
                log.info("Unpublished {} session templates from group {} in the authorization engine", request.getSessionTemplateIdsToRemove().size(), userGroupId);
            }

            if (request.getSessionTemplateIdsToAdd() != null) {
                for (String sessionTemplateId : request.getSessionTemplateIdsToAdd()) {
                    authorizationEngine.addPrincipalToSharedList(PrincipalType.Group, userGroupId, ResourceType.SessionTemplate, sessionTemplateId, ShareLevel.publishedTo);
                }
                log.info("Published {} session templates to group {} in the authorization engine", request.getSessionTemplateIdsToAdd().size(), userGroupId);
            }

            EditUserGroupResponse response = new EditUserGroupResponse();
            response.userGroup(editedUserGroup);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, EDIT_USER_GROUP_DEFAULT_MESSAGE);
        }
    }
}
