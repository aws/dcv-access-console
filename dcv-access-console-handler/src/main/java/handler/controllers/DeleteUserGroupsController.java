package handler.controllers;

import handler.api.DeleteUserGroupsApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.DeleteUserGroupsRequestData;
import handler.model.DeleteUserGroupsResponse;
import handler.model.Error;
import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.UserGroupUser;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.DeleteUserGroupsErrors.DELETE_USER_GROUP_DEFAULT_MESSAGE;
import static handler.errors.DeleteUserGroupsErrors.MISSING_REQUIRED_PARAMETERS_ERROR;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeleteUserGroupsController implements DeleteUserGroupsApi {
    private final AbstractAuthorizationEngine authorizationEngine;
    private final UserGroupService userGroupService;
    private final SessionTemplateService sessionTemplateService;

    private ResponseEntity<DeleteUserGroupsResponse> sendExceptionResponse(HttpStatus status, Exception e, DeleteUserGroupsRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing deleteUserGroups for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DeleteUserGroupsResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DeleteUserGroupsResponse> deleteUserGroups(DeleteUserGroupsRequestData request) {
        try {
            log.info("Received deleteUserGroup request: {}", request);

            if (request.getUserGroupIds() == null || request.getDeleteIfNotEmpty() == null) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException(MISSING_REQUIRED_PARAMETERS_ERROR.getDescription()), request, MISSING_REQUIRED_PARAMETERS_ERROR);
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            DeleteUserGroupsResponse response = new DeleteUserGroupsResponse();

            for (String userGroupId : request.getUserGroupIds()) {
                if (!authorizationEngine.isAuthorized(PrincipalType.User, username,
                        ResourceAction.deleteUserGroup, ResourceType.Group, userGroupId)) {
                    log.warn("User {} is not authorized to delete User Group {}", username, userGroupId);
                    response.addUnsuccessfulListItem(userGroupId);
                    continue;
                }
                log.info("User {} is authorized to delete User Group {}", username, userGroupId);

                List<String> members = userGroupService.getUserIdsForGroup(userGroupId);
                log.info("Found {} members in group {}", members.size(), userGroupId);

                // Handle clearing the relationships between users and groups
                if (!members.isEmpty()) {
                    if (!request.getDeleteIfNotEmpty()) {
                        log.warn("Unable to delete group {} as it is not empty, and deleteIfNotEmpty is false", userGroupId);
                        response.addUnsuccessfulListItem(userGroupId);
                        continue;
                    }

                    log.info("Removing users from group {}", userGroupId);

                    List<UserGroupUser> userGroupUsersToRemove = new ArrayList<>();
                    for (String userId : members) {
                        UserGroupUser userGroupUser = new UserGroupUser();
                        userGroupUser.setUserGroupId(userGroupId);
                        userGroupUser.setUserId(userId);

                        userGroupUsersToRemove.add(userGroupUser);

                        authorizationEngine.removeUserFromGroup(userId, userGroupId);
                    }

                    userGroupService.removeMembersFromGroup(userGroupUsersToRemove);
                }

                // Handle clearing the relationships between groups and session templates
                List<SessionTemplatePublishedToUserGroup> sessionTemplatesPublished = sessionTemplateService.getSessionTemplatesPublishedToUserGroup(userGroupId);
                for (SessionTemplatePublishedToUserGroup sessionTemplate : sessionTemplatesPublished) {
                    authorizationEngine.removePrincipalFromSharedList(PrincipalType.Group, userGroupId, ResourceType.SessionTemplate, sessionTemplate.getId().getSessionTemplateId(), ShareLevel.publishedTo);
                }
                if (!sessionTemplatesPublished.isEmpty()) {
                    log.info("Removing {} relationships between User Group {} and Session Templates", sessionTemplatesPublished.size(), userGroupId);
                    sessionTemplateService.removeUserGroupsFromSessionTemplate(sessionTemplatesPublished);
                } else {
                    log.info("No session templates were found for User Group {}", userGroupId);
                }

                if (authorizationEngine.removeGroup(userGroupId)) {
                    response.addSuccessfulListItem(userGroupId);
                } else {
                    response.addUnsuccessfulListItem(userGroupId);
                }
            }
            if (!CollectionUtils.isEmpty(response.getSuccessfulList())) {
                userGroupService.deleteUserGroups(response.getSuccessfulList());
            } else {
                log.warn("No user groups were able to be deleted");
            }

            log.info("Successfully sent deleteUserGroups response: {}", response);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DELETE_USER_GROUP_DEFAULT_MESSAGE);
        }
    }
}
