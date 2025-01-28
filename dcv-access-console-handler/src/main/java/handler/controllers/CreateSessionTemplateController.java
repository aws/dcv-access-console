// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.services.SessionTemplateService;
import handler.api.CreateSessionTemplateApi;
import handler.model.CreateSessionTemplateResponse;
import handler.model.CreateSessionTemplateRequestData;
import handler.model.SessionTemplate;
import handler.model.Error;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static handler.errors.CommonErrorsEnum.BROKER_AUTHENTICATION_ERROR;
import static handler.errors.CreateSessionTemplateErrors.AUTHORIZATION_ENGINE_FAILED_TO_SAVE_TEMPLATE_ERROR;
import static handler.errors.CreateSessionTemplateErrors.CREATE_SESSION_TEMPLATE_DEFAULT_MESSAGE;

@AllArgsConstructor
@RestController
@Slf4j
public class CreateSessionTemplateController implements CreateSessionTemplateApi {
    private SessionTemplateService sessionTemplateService;
    private final AbstractAuthorizationEngine authorizationEngine;


    private ResponseEntity<CreateSessionTemplateResponse> sendExceptionResponse(HttpStatus status, Exception e, CreateSessionTemplateRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing createSessionTemplate for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value()));
        return new ResponseEntity<>(new CreateSessionTemplateResponse().error(error.message(errorMessage.getDescription())), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<CreateSessionTemplateResponse> createSessionTemplate(CreateSessionTemplateRequestData request) {
        try {
            log.info("Received createSessionTemplate request: {}", request);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            CreateSessionTemplateResponse response = sessionTemplateService.saveSessionTemplate(new SessionTemplate(), request, false, username);
            if(!authorizationEngine.addSessionTemplate(response.getSessionTemplate().getId(), username)) {
                sessionTemplateService.deleteSessionTemplate(response.getSessionTemplate().getId());
                String message = String.format("Unable to create Session Template in authorization engine: %s", response);
                return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException(message), request, AUTHORIZATION_ENGINE_FAILED_TO_SAVE_TEMPLATE_ERROR);
            }

            log.info("Successfully sent createSessionTemplate response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BROKER_AUTHENTICATION_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, CREATE_SESSION_TEMPLATE_DEFAULT_MESSAGE);
        }
    }
}
