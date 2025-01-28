// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;


import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.errors.HandlerErrorMessage;
import handler.model.*;
import handler.model.Error;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import handler.api.GetSessionScreenshotsApi;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BrokerAuthenticationException;
import handler.exceptions.BadRequestException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.CommonErrorsEnum.BROKER_AUTHENTICATION_ERROR;
import static handler.errors.GetSessionScreenshotsErrors.GET_SESSION_SCREENSHOTS_DEFAULT_MESSAGE;
import static handler.errors.GetSessionScreenshotsErrors.GET_SESSION_SCREENSHOTS_NO_IDS;

@Slf4j
@RestController
@AllArgsConstructor
public class GetSessionScreenshotsController implements GetSessionScreenshotsApi {

    private final static String SESSION_ID_REGEX = "^(?:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}|console)$";

    private final BrokerClient brokerClient;
    private AbstractAuthorizationEngine authorizationEngine;

    private ResponseEntity<GetSessionScreenshotsUIResponse> sendExceptionResponse(HttpStatus status, Exception e, GetSessionScreenshotsUIRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing getSessionScreenshots for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new GetSessionScreenshotsUIResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<GetSessionScreenshotsUIResponse> getSessionScreenshots(GetSessionScreenshotsUIRequestData request) {
        try {
            log.info("Received getSessionScreenshots request: {}", request);

            if(request.getSessionIds() == null) {
                return sendExceptionResponse(HttpStatus.BAD_REQUEST, new BadRequestException(GET_SESSION_SCREENSHOTS_NO_IDS.getDescription()), request, GET_SESSION_SCREENSHOTS_NO_IDS);
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            GetSessionScreenshotsUIResponse response = new GetSessionScreenshotsUIResponse();

            boolean isAdmin = false;
            if(authorizationEngine.getUserRole(username).equals("Admin")) {
                log.info("Admin {} is authorized to view session screenshots for any sessions", username);
                isAdmin = true;
            }

            List<String> badSessionIds = new ArrayList<>();

            ListIterator<String> iter = request.getSessionIds().listIterator();
            while (iter.hasNext()) {
                String sessionId = iter.next();
                if (!sessionId.matches(SESSION_ID_REGEX)) {
                    iter.remove();
                    badSessionIds.add(sessionId);
                }
            }

            if(!isAdmin) {
                GetSessionScreenshotsUIRequestData getSessionScreenshotsUIRequestData = new GetSessionScreenshotsUIRequestData();
                List<GetSessionScreenshotUnsuccessfulResponse> unauthorizedList = new ArrayList<>();
                for (String sessionId : request.getSessionIds()) {
                    if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.viewSessionScreenshotDetails, ResourceType.Session, sessionId)) {
                        String msg = String.format("User %s is not authorized to view session screenshot details for session %s", username, sessionId);
                        log.warn(msg);
                        unauthorizedList.add(new GetSessionScreenshotUnsuccessfulResponse().failureReason(msg).getSessionScreenshotRequestData(new GetSessionScreenshotRequestData().sessionId(sessionId)));
                    } else {
                        log.info("User {} is authorized to view session screenshots for session {}", username, sessionId);
                        getSessionScreenshotsUIRequestData.addSessionIdsItem(sessionId);
                    }
                }
                if(!getSessionScreenshotsUIRequestData.getSessionIds().isEmpty()) {
                    getSessionScreenshotsUIRequestData.setMaxWidth(request.getMaxWidth());
                    getSessionScreenshotsUIRequestData.setMaxHeight(request.getMaxHeight());
                    response = brokerClient.getSessionScreenshots(getSessionScreenshotsUIRequestData);
                    unauthorizedList.forEach(response::addUnsuccessfulListItem);
                }
            } else if(!request.getSessionIds().isEmpty()) {
                response = brokerClient.getSessionScreenshots(request);
            }

            for (String ignored : badSessionIds) {
                response.addUnsuccessfulListItem(new GetSessionScreenshotUnsuccessfulResponse().failureReason("Invalid Session ID"));
            }

            // Not logging entire response because it exposes the screenshot
            log.info("Successfully sent getSessionScreenshots request id: {}", response.getRequestId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (BrokerAuthenticationException | UsernameNotFoundException e) {
            return sendExceptionResponse(HttpStatus.UNAUTHORIZED, e, request, BROKER_AUTHENTICATION_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, GET_SESSION_SCREENSHOTS_DEFAULT_MESSAGE);
        }
    }
}
