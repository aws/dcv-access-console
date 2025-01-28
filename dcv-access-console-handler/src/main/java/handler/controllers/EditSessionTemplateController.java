// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.EditSessionTemplateApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.CreateSessionTemplateResponse;
import handler.model.EditSessionTemplateRequestData;
import handler.model.SessionTemplate;
import handler.model.Error;
import handler.services.SessionTemplateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.EditSessionTemplateErrors.EDIT_SESSION_TEMPLATE_DEFAULT_MESSAGE;
import static handler.errors.EditSessionTemplateErrors.MISSING_REQUEST_DATA;
import static handler.errors.EditSessionTemplateErrors.MISSING_TEMPLATE_ID;
import static handler.errors.EditSessionTemplateErrors.USER_NOT_AUTHORIZED_TO_EDIT_TEMPLATE;

@AllArgsConstructor
@RestController
@Slf4j
public class EditSessionTemplateController implements EditSessionTemplateApi {
    private SessionTemplateService sessionTemplateService;
    private final AbstractAuthorizationEngine authorizationEngine;

    private ResponseEntity<CreateSessionTemplateResponse> sendExceptionResponse(HttpStatus status, Exception e, EditSessionTemplateRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing editSessionTemplate for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new CreateSessionTemplateResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<CreateSessionTemplateResponse> editSessionTemplate(EditSessionTemplateRequestData request) {
        try {
            log.info("Received editSessionTemplate request: {}", request);

            if (request.getTemplateId() == null) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException("TemplateId is required"), request, MISSING_TEMPLATE_ID);
            }

            if (request.getCreateSessionTemplateRequestData() == null) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException("CreateSessionTemplateRequestData is required"), request, MISSING_REQUEST_DATA);
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.editSpecificSessionTemplate, ResourceType.SessionTemplate, request.getTemplateId())) {
                String message = String.format("User %s is not authorized to edit session template %s", username, request.getTemplateId());
                return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new AuthorizationServiceException(message), request, USER_NOT_AUTHORIZED_TO_EDIT_TEMPLATE);
            }

            SessionTemplate sessionTemplate = sessionTemplateService.getUpdatedNameSessionTemplate(request.getTemplateId(), request.getCreateSessionTemplateRequestData().getName());
            boolean nameHasChanged = sessionTemplate.getId() == null;

            CreateSessionTemplateResponse response = sessionTemplateService.saveSessionTemplate(sessionTemplate, request.getCreateSessionTemplateRequestData(), !nameHasChanged, username);
            authorizationEngine.addSessionTemplate(response.getSessionTemplate().getId(), username);

            if (nameHasChanged) {
                List<String> userIds = authorizationEngine.getSharedListForResource(ResourceType.SessionTemplate, request.getTemplateId(), ShareLevel.publishedTo, PrincipalType.User);
                List<String> userGroupIds = authorizationEngine.getSharedListForResource(ResourceType.SessionTemplate, request.getTemplateId(), ShareLevel.publishedTo, PrincipalType.Group);
                authorizationEngine.setShareList(userIds, userGroupIds, ResourceType.SessionTemplate, response.getSessionTemplate().getId(), ShareLevel.publishedTo);
                authorizationEngine.setShareList(Collections.emptyList(), Collections.emptyList(), ResourceType.SessionTemplate, request.getTemplateId(), ShareLevel.publishedTo);

                sessionTemplateService.deleteSessionTemplate(request.getTemplateId());
                authorizationEngine.deleteResource(ResourceType.SessionTemplate, request.getTemplateId());
            }

            log.info("Successfully sent editSessionTemplate response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, EDIT_SESSION_TEMPLATE_DEFAULT_MESSAGE);
        }
    }
}
