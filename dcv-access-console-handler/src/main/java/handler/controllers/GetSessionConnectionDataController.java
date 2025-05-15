// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.GetSessionConnectionDataApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.SystemAction;
import handler.brokerclients.BrokerClient;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.model.Error;
import handler.model.GetSessionConnectionDataUIResponse;
import handler.model.KeyValuePair;
import handler.model.Server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.CommonErrorsEnum.BROKER_AUTHENTICATION_ERROR;
import static handler.errors.GetSessionConnectionDataErrors.GET_SESSION_CONNECTION_DATA_DEFAULT_MESSAGE;
import static handler.errors.GetSessionConnectionDataErrors.UNAUTHORIZED_TO_CONNECT_AS_OTHER;
import static handler.errors.GetSessionConnectionDataErrors.UNAUTHORIZED_TO_CONNECT_TO_SESSION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetSessionConnectionDataController implements GetSessionConnectionDataApi {
    private final BrokerClient brokerClient;
    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${enable-connection-gateway:false}")
    private Boolean gatewayEnabled;

    @Value("${connection-gateway-host:gatewayhostname}")
    private String gatewayHostname;

    @Value("${connection-gateway-port:8443}")
    private String gatewayPort;

    @Value("${enable-public-ip-from-tag:true}")
    private Boolean publicIpEnabled;

    @Value("${public-ip-tag-name:public_ipv4}")
    private String publicIpTag;

    private ResponseEntity<GetSessionConnectionDataUIResponse> sendExceptionResponse(HttpStatus status, Exception e, String sessionId, HandlerErrorMessage errorMessage) {
        log.error("Error while performing getSessionConnectionData for sessionId: {}", sessionId, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new GetSessionConnectionDataUIResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<GetSessionConnectionDataUIResponse> getSessionConnectionData(String sessionId, String user) {
        try {
            log.info("Received getSessionConnectionData for sessionId: {}", sessionId);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if (StringUtils.isNotBlank(user) && !user.equals(authorizationEngine.getUserLoginUsername(username))) {
                // Owner is not empty, and is not the current user
                if (!authorizationEngine.isAuthorized(PrincipalType.User, username, SystemAction.getSessionConnectionDataForOther)) {
                    String message = String.format("User %s is not authorized to connect to other users sessions.", username);
                    return sendExceptionResponse(HttpStatus.UNAUTHORIZED, new AuthorizationServiceException(message), sessionId, UNAUTHORIZED_TO_CONNECT_AS_OTHER);
                }
                log.info("User {} was authorized to connect to session {}...", username, sessionId);
            }

            if (!authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.connectToSession,
                    ResourceType.Session, sessionId)) {
                String message = String.format("User %s is not authorized to connect to session %s.", username, sessionId);
                return sendExceptionResponse(HttpStatus.UNAUTHORIZED,
                        new AuthorizationServiceException(message), sessionId, UNAUTHORIZED_TO_CONNECT_TO_SESSION);
            }

            GetSessionConnectionDataUIResponse response = brokerClient.getSessionConnectionData(sessionId, Optional.ofNullable(user).orElse(authorizationEngine.getUserLoginUsername(username)));
            Server server = response.getSession().getServer();
            String hostName = getHostName(server);
            String port = getPort(server);
            String hostNameWithoutProtocol;

            if (hostName.contains("://")) {
                hostNameWithoutProtocol = hostName.split("://")[1];
            } else {
                hostNameWithoutProtocol = hostName;
                hostName = String.format("https://%s", hostName);
            }

            // Set the web connection url
            String webConnectionUrl = "%s:%s%s?authToken=%s#%s".formatted(hostName, port,
                    server.getWebUrlPath(), response.getConnectionToken(), response.getSession().getId());
            response.setWebConnectionUrl(webConnectionUrl);

            // Set the native connection url
            String nativeConnectionUrl = "dcv://%s:%s%s?authToken=%s#%s".formatted(hostNameWithoutProtocol, port,
                    server.getWebUrlPath(), response.getConnectionToken(), response.getSession().getId());
            response.setNativeConnectionUrl(nativeConnectionUrl);

            // Not logging entire response because it exposes the connection data
            log.info("Successfully sent getSessionConnectionData for session id: {}", response.getSession().getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, sessionId, BAD_REQUEST_ERROR);
        } catch (BrokerAuthenticationException e) {
            return sendExceptionResponse(HttpStatus.UNAUTHORIZED, e, sessionId, BROKER_AUTHENTICATION_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, sessionId, GET_SESSION_CONNECTION_DATA_DEFAULT_MESSAGE);
        }
    }

    protected String getHostName(Server server) {
        String hostName = gatewayHostname;
        if (!gatewayEnabled) {
            hostName = server.getIp();
            if (publicIpEnabled && server.getTags() != null) {
                for (KeyValuePair tag : server.getTags()) {
                    if (publicIpTag.equals(tag.getKey())) {
                        hostName = tag.getValue();
                        break;
                    }
                }
            }
        }
        return hostName;
    }

    protected String getPort(Server server) {
        return gatewayEnabled ? gatewayPort : server.getPort();
    }

}
