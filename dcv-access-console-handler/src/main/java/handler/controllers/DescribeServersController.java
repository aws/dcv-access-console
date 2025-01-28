// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.api.DescribeServersApi;
import handler.errors.HandlerErrorMessage;
import handler.model.DescribeServersUIRequestData;
import handler.model.DescribeServersUIResponse;
import handler.model.Server;
import handler.model.Error;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerAuthenticationException;
import handler.utils.Filter;
import handler.utils.Sort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.CommonErrorsEnum.BROKER_AUTHENTICATION_ERROR;
import static handler.errors.DescribeServersErrors.DESCRIBE_SERVERS_DEFAULT_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeServersController implements DescribeServersApi {
    private final BrokerClient brokerClient;
    private final Filter<DescribeServersUIRequestData, Server> serverFilter;
    private final Sort<DescribeServersUIRequestData, Server> serverSort;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    private ResponseEntity<DescribeServersUIResponse> sendExceptionResponse(HttpStatus status, Exception e, DescribeServersUIRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing describeServers for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DescribeServersUIResponse().error(error.message(e.getMessage())), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DescribeServersUIResponse> describeServers(DescribeServersUIRequestData request) {
        try {
            log.info("Received describeServers request: {}", request);

            int resultsRemaining = request.getMaxResults() != null ? request.getMaxResults() : defaultMaxResults;
            request.setMaxResults(resultsRemaining);
            List<Server> servers = new ArrayList<>();
            DescribeServersUIResponse response;

            do {
                response = brokerClient.describeServers(request);

                List<Server> newServers = serverFilter.getFiltered(request, response.getServers());
                servers.addAll(newServers);

                resultsRemaining -= newServers.size();
                request.setMaxResults(resultsRemaining);
                request.setNextToken(response.getNextToken());
            } while (resultsRemaining > 0 && response.getNextToken() != null);

            servers = serverSort.getSorted(request, servers);
            response.setServers(servers);

            log.info("Successfully sent describeServers response of size {}", response.getServers().size());
            log.debug("Full response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        }
        catch (BrokerAuthenticationException e) {
            return sendExceptionResponse(HttpStatus.UNAUTHORIZED, e, request, BROKER_AUTHENTICATION_ERROR);
        }
        catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DESCRIBE_SERVERS_DEFAULT_MESSAGE);
        }
    }
}
