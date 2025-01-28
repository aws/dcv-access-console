// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.engines.entities.SetShareListResponse;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.api.PublishSessionTemplateApi;
import handler.model.PublishSessionTemplateResponse;
import handler.model.PublishSessionTemplateRequestData;
import handler.model.Error;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.PublishSessionTemplateErrors.PUBLISH_SESSION_TEMPLATE_DEFAULT_MESSAGE;
import static handler.errors.PublishSessionTemplateErrors.PUBLISH_SESSION_TEMPLATE_NO_IDS;
import static handler.errors.PublishSessionTemplateErrors.USER_UNAUTHORIZED_TO_PUBLISH_SESSION_TEMPLATE;

@AllArgsConstructor
@RestController
@Slf4j
public class PublishSessionTemplateController implements PublishSessionTemplateApi {
    private AbstractAuthorizationEngine authorizationEngine;

    private ResponseEntity<PublishSessionTemplateResponse> sendExceptionResponse(HttpStatus status, Exception e, PublishSessionTemplateRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing publishSessionTemplate for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new PublishSessionTemplateResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<PublishSessionTemplateResponse> publishSessionTemplate(PublishSessionTemplateRequestData request) {
        try {
            log.info("Received publishSessionTemplate request: {}", request);

            if(request.getId() == null) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException("Publish SessionTemplates failed: No Id provided"), request, PUBLISH_SESSION_TEMPLATE_NO_IDS);
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.publishSpecificSessionTemplate, ResourceType.SessionTemplate, request.getId())) {
                String message = String.format("User %s is not authorized to publish Session Template %s", username, request.getId());
                return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new RuntimeException(message), request, USER_UNAUTHORIZED_TO_PUBLISH_SESSION_TEMPLATE);
            }

            List<String> userIds = request.getUserIds() != null ? request.getUserIds() : Collections.emptyList();
            List<String> groupIds = request.getGroupIds() != null ? request.getGroupIds() : Collections.emptyList();
            PublishSessionTemplateResponse response = new PublishSessionTemplateResponse();
            try {
                SetShareListResponse setShareListResponse = authorizationEngine.setShareList(userIds, groupIds,
                        ResourceType.SessionTemplate, request.getId(), ShareLevel.publishedTo);
                response.setSuccessfulUsersList(setShareListResponse.getSuccessfulUsers());
                response.setUnsuccessfulUsersList(setShareListResponse.getUnSuccessfulUsers());
                response.setSuccessfulGroupsList(setShareListResponse.getSuccessfulGroups());
                response.setUnsuccessfulGroupsList(setShareListResponse.getUnSuccessfulGroups());
            } catch (Exception e) {
                log.warn("Unable to update share list for SessionTemplate {}", request.getId(), e);
                response.setSuccessfulUsersList(Collections.emptyList());
                response.setSuccessfulGroupsList(Collections.emptyList());
                response.setUnsuccessfulUsersList(userIds);
                response.setUnsuccessfulGroupsList(groupIds);
            }

            log.info("Successfully sent publishSessionTemplate response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, PUBLISH_SESSION_TEMPLATE_DEFAULT_MESSAGE);
        }
    }
}
