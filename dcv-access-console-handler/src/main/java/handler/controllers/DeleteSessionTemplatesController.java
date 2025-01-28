// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.DeleteSessionTemplatesApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.engines.entities.SetShareListResponse;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.services.SessionTemplateService;
import handler.model.DeleteSessionTemplatesRequestData;
import handler.model.DeleteSessionTemplatesResponse;
import handler.model.Error;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.DeleteSessionTemplatesErrors.DELETE_SESSION_TEMPLATES_DEFAULT_MESSAGE;
import static handler.errors.DeleteSessionTemplatesErrors.EMPTY_IDS_ERROR;

@AllArgsConstructor
@RestController
@Slf4j
public class DeleteSessionTemplatesController implements DeleteSessionTemplatesApi {
    private SessionTemplateService sessionTemplateService;
    private final AbstractAuthorizationEngine authorizationEngine;

    private ResponseEntity<DeleteSessionTemplatesResponse> sendExceptionResponse(HttpStatus status, Exception e, DeleteSessionTemplatesRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing deleteSessionTemplates for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DeleteSessionTemplatesResponse().error(error.message("Error while deleting session template(s)")), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DeleteSessionTemplatesResponse> deleteSessionTemplates(DeleteSessionTemplatesRequestData request) {
        try {
            log.info("Received deleteSessionTemplates request: {}", request);

            if(request.getIds() == null) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException("No Ids provided"), request, EMPTY_IDS_ERROR);
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<String> filteredSessionTemplates = new ArrayList<>();
            DeleteSessionTemplatesResponse response = new DeleteSessionTemplatesResponse();

            for (String id : request.getIds()) {
                if (!authorizationEngine.isAuthorized(PrincipalType.User, username,
                        ResourceAction.deleteSessionTemplate, ResourceType.SessionTemplate, id)) {
                    log.warn("User {} is not authorized to delete Session Template {}", username, id);
                    continue;
                }
                log.info("User {} is authorized to delete Session Template {}", username, id);

                //Remove share list
                try {
                    SetShareListResponse setShareListResponse = authorizationEngine.setShareList(new ArrayList<>(), new ArrayList<>(), ResourceType.SessionTemplate, id, ShareLevel.publishedTo);
                    if(setShareListResponse.getUnSuccessfulUsers().isEmpty() && setShareListResponse.getUnSuccessfulGroups().isEmpty()) {
                        filteredSessionTemplates.add(id);
                    }
                } catch (Exception e) {
                    log.warn("Unable to remove share list for SessionTemplate {}", id, e);
                }
            }

            sessionTemplateService.deleteSessionTemplates(filteredSessionTemplates);
            for(String id: filteredSessionTemplates) {
                response.addSuccessfulListItem(id);
                authorizationEngine.deleteResource(ResourceType.SessionTemplate, id);
            }
            request.getIds().stream().filter(id -> !filteredSessionTemplates.contains(id)).forEach(response::addUnsuccessfulListItem);

            log.info("Successfully sent deleteSessionTemplates response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DELETE_SESSION_TEMPLATES_DEFAULT_MESSAGE);
        }
    }
}
