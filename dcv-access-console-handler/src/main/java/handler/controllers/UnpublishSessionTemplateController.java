// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.services.SessionTemplateService;
import handler.api.UnpublishSessionTemplateApi;
import handler.model.UnpublishSessionTemplateResponse;
import handler.model.UnpublishSessionTemplateRequestData;
import handler.model.Error;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.UnpublishSessionTemplateErrors.UNPUBLISH_SESSION_TEMPLATE_DEFAULT_MESSAGE;
import static handler.errors.UnpublishSessionTemplateErrors.UNPUBLISH_SESSION_TEMPLATE_NO_IDS;
import static handler.errors.UnpublishSessionTemplateErrors.USER_UNAUTHORIZED_TO_UNPUBLISH_SESSION_TEMPLATE;

@AllArgsConstructor
@RestController
@Slf4j
public class UnpublishSessionTemplateController implements UnpublishSessionTemplateApi {
    private SessionTemplateService sessionTemplateService;
    private AbstractAuthorizationEngine authorizationEngine;

    private ResponseEntity<UnpublishSessionTemplateResponse> sendExceptionResponse(HttpStatus status, Exception e, UnpublishSessionTemplateRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing unpublishSessionTemplate for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new UnpublishSessionTemplateResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<UnpublishSessionTemplateResponse> unpublishSessionTemplate(UnpublishSessionTemplateRequestData request) {
        try {
            log.info("Received unpublishSessionTemplate request: {}", request);

            if (StringUtils.isEmpty(request.getId())) {
                String message = "Request must include session template ID";
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException(message), request, UNPUBLISH_SESSION_TEMPLATE_NO_IDS);
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.unpublishSpecificSessionTemplate, ResourceType.SessionTemplate, request.getId())) {
                log.warn("User {} is not authorized to unpublish session template {}", username, request.getId());
                String message = String.format("User %s is not authorized to unpublish Session Template %s", username, request.getId());
                return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new AuthorizationServiceException(message), request, USER_UNAUTHORIZED_TO_UNPUBLISH_SESSION_TEMPLATE);
            }

            UnpublishSessionTemplateResponse response = sessionTemplateService.unpublishSessionTemplate(request.getId(), request.getUserIds(), request.getGroupIds());

            log.info("Successfully sent unpublishSessionTemplate response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, UNPUBLISH_SESSION_TEMPLATE_DEFAULT_MESSAGE);
        }
    }
}
