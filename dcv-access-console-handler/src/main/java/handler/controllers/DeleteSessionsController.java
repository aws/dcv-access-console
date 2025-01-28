// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.DeleteSessionsApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.brokerclients.BrokerClient;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.model.DeleteSessionUIRequestData;
import handler.model.DeleteSessionsUIResponse;
import handler.model.Error;
import handler.model.UnsuccessfulDeleteSessionResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.CommonErrorsEnum.BROKER_AUTHENTICATION_ERROR;
import static handler.errors.DeleteSessionsErrors.DELETE_SESSIONS_DEFAULT_MESSAGE;

@Slf4j
@AllArgsConstructor
@RestController
public class DeleteSessionsController implements DeleteSessionsApi {
    private BrokerClient brokerClient;
    private AbstractAuthorizationEngine authorizationEngine;

    private ResponseEntity<DeleteSessionsUIResponse> sendExceptionResponse(HttpStatus status, Exception e, List<DeleteSessionUIRequestData> requests, HandlerErrorMessage errorMessage) {
        log.error("Error while performing deleteSessions. Request: {}", requests, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DeleteSessionsUIResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DeleteSessionsUIResponse> deleteSessions(List<DeleteSessionUIRequestData> deleteSessionsUIRequestData) {
        try {
            log.info("Received deleteSession request: {}", deleteSessionsUIRequestData);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean isAdmin = false;
            if(authorizationEngine.getUserRole(username).equals("Admin")) {
                log.info("Admin {} is authorized to delete any Session Template", username);
                isAdmin = true;
            }
            List<UnsuccessfulDeleteSessionResponse> unsuccessfulResponses = new ArrayList<>();
            DeleteSessionsUIResponse response = new DeleteSessionsUIResponse().successfulList(new ArrayList<>()).unsuccessfulList(unsuccessfulResponses);

            if(!isAdmin) {
                List<DeleteSessionUIRequestData> brokerRequests = new ArrayList<>();
                for (DeleteSessionUIRequestData request : deleteSessionsUIRequestData) {
                    if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.deleteSession, ResourceType.Session, request.getSessionId())) {
                        log.warn("User {} is not authorized to delete session {}", username, request.getSessionId());
                        HashMap<String, String> errors = new HashMap<>();
                        errors.put("Unauthorized", String.format("User %s is not authorized to delete session %s", username, request.getSessionId()));
                        unsuccessfulResponses.add(new UnsuccessfulDeleteSessionResponse().failureReasons(errors).sessionId(request.getSessionId()));
                        continue;
                    }
                    log.warn("User {} is authorized to delete session {}", username, request.getSessionId());
                    brokerRequests.add(request);
                }
                if(!brokerRequests.isEmpty()) {
                    response = brokerClient.deleteSessions(brokerRequests);
                    unsuccessfulResponses.forEach(response::addUnsuccessfulListItem);
                }
            } else if(!deleteSessionsUIRequestData.isEmpty()) {
                response = brokerClient.deleteSessions(deleteSessionsUIRequestData);
            }
            response.getSuccessfulList().forEach(successfulDeleteSessionResponse -> authorizationEngine.deleteResource(ResourceType.Session, successfulDeleteSessionResponse.getSessionId()));

            log.info("Successfully sent deleteSession response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, deleteSessionsUIRequestData, BAD_REQUEST_ERROR);
        } catch (BrokerAuthenticationException | UsernameNotFoundException e) {
            return sendExceptionResponse(HttpStatus.UNAUTHORIZED, e, deleteSessionsUIRequestData, BROKER_AUTHENTICATION_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, deleteSessionsUIRequestData, DELETE_SESSIONS_DEFAULT_MESSAGE);
        }
    }
}
