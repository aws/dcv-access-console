// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.brokerclients.dcv;

import broker.ApiException;
import broker.api.GetSessionConnectionDataApi;
import broker.api.ServersApi;
import broker.api.SessionsApi;
import broker.model.CreateSessionsResponse;
import broker.model.DeleteSessionRequestData;
import broker.model.DeleteSessionUnsuccessfulResponse;
import broker.model.DeleteSessionsResponse;
import broker.model.DescribeServersRequestData;
import broker.model.DescribeServersResponse;
import broker.model.DescribeSessionsRequestData;
import broker.model.DescribeSessionsResponse;
import broker.model.GetSessionScreenshotRequestData;
import broker.model.Host;
import broker.model.Server;
import broker.model.Session;
import broker.model.UnsuccessfulCreateSessionRequestData;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerClientException;
import handler.model.CreateSessionRequestData;
import handler.model.CreateSessionTemplateRequestData;
import handler.model.CreateSessionUIRequestData;
import handler.model.CreateSessionsUIResponse;
import handler.model.DeleteSessionUIRequestData;
import handler.model.DeleteSessionsUIResponse;
import handler.model.DescribeServersUIRequestData;
import handler.model.DescribeServersUIResponse;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.DescribeSessionsUIResponse;
import handler.model.FilterToken;
import handler.model.GetSessionConnectionDataUIResponse;
import handler.model.GetSessionScreenshotsUIRequestData;
import handler.model.GetSessionScreenshotsUIResponse;
import handler.model.OsFamily;
import handler.model.SessionTemplate;
import handler.model.Type;
import handler.model.UnsuccessfulCreateSessionUIRequestData;
import handler.model.UnsuccessfulDeleteSessionResponse;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.utils.requirements.JavaccRequirementsParserMatcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class DCVBrokerClient extends BrokerClient {
    private ObjectProvider<SessionsApi> sessionsApiProvider;
    private ObjectProvider<ServersApi> serversApiProvider;
    private ObjectProvider<GetSessionConnectionDataApi> getSessionConnectionDataApiProvider;
    private DCVBrokerHandlerMapper mapper;
    private DCVBrokerTokenClient tokenClient;
    private ObjectMapper objectMapper;
    private final PagingAndSortingCrudRepository<SessionTemplate, String> sessionTemplateRepository;

    private SessionsApi getSessionsApi() {
        SessionsApi sessionsApi = sessionsApiProvider.getIfAvailable();
        sessionsApi.getApiClient().setBearerToken(tokenClient.getToken());
        return sessionsApi;
    }

    @Override
    public DescribeSessionsUIResponse describeSessions(DescribeSessionsUIRequestData request) {
        try {
            DescribeSessionsRequestData brokerRequest = new DescribeSessionsRequestData();
            brokerRequest.setMaxResults(request.getMaxResults());
            brokerRequest.setNextToken(request.getNextToken());
            brokerRequest.setFilters(mapper.mapToBrokerKeyValuePairs(request.getTags()));

            if(request.getSessionIds() != null) {
                for (FilterToken token : request.getSessionIds()) {
                    if (FilterToken.OperatorEnum.EQUAL.equals(token.getOperator())) {
                        brokerRequest.addSessionIdsItem(token.getValue());
                    }
                }
            }

            DescribeSessionsResponse describeSessionsResponse = getSessionsApi().describeSessions(brokerRequest);
            populateSessionsWithHostInfo(describeSessionsResponse);

            return mapper.mapToDescribeSessionsUIResponse(describeSessionsResponse);
        }
        catch (ApiException e) {
            if(e.getCode() == 400) {
                throw new BadRequestException(e);
            }
            throw new BrokerClientException(e);
        }
    }

    private void populateSessionsWithHostInfo(DescribeSessionsResponse describeSessionsResponse) {
        if (describeSessionsResponse == null) {
            log.warn("Unable to populate server info, as broker response is null");
            return;
        }

        // Map should contain an index from each server ID to the index of which session requires it
        Map<String, List<Integer>> serverInfoMap = new HashMap<>();
        List<Session> sessions = describeSessionsResponse.getSessions();

        if (sessions == null) {
            log.warn("Unable to populate server info for describeSessions response {}, as sessions is null", describeSessionsResponse);
            return;
        }

        // Loop through each session and add the index of the session to the map
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            if (session == null) {
                log.warn("Unable to populate server info for session {} in describeSessions response {}, as the session is null", i, describeSessionsResponse);
                continue;
            }
            if (session.getServer() == null) {
                log.warn("Unable to retrieve server info for session {} in describeSessions response {}, response didn't contain host ID", session, describeSessionsResponse);
                continue;
            }
            if (!serverInfoMap.containsKey(session.getServer().getId())) {
                serverInfoMap.put(session.getServer().getId(), new ArrayList<>());
            }
            serverInfoMap.get(session.getServer().getId()).add(i);
        }

        // Get all the servers that we've added to the map
        DescribeServersRequestData describeServersRequest = new DescribeServersRequestData();
        describeServersRequest.serverIds(serverInfoMap.keySet().stream().toList());
        DescribeServersResponse brokerResponse = describeServers(describeServersRequest);

        // For each server, get all the sessions that need that server info and populate each session object with it
        for (Server server : brokerResponse.getServers()) {
            if (serverInfoMap.get(server.getId()) == null) {
                log.warn("Unable to retrieve server info for server {} in describeSessions response {}, response didn't contain host ID", server, describeSessionsResponse);
                continue;
            }
            for (int sessionIndex : serverInfoMap.get(server.getId())) {
                describeSessionsResponse.getSessions().get(sessionIndex).server(server);
            }
        }
    }

    @Override
    public GetSessionScreenshotsUIResponse getSessionScreenshots(GetSessionScreenshotsUIRequestData request) {
        try {
            List<GetSessionScreenshotRequestData> brokerRequestList = new ArrayList<>();
            if (request.getSessionIds() != null) {
                if (request.getMaxWidth() == 0 || request.getMaxHeight() == 0) {
                    return getSessionScreenshotsWithBrokerConfig(request);
                }
                for (String sessionId : request.getSessionIds()) {
                    GetSessionScreenshotRequestData brokerRequest = new GetSessionScreenshotRequestData();
                    brokerRequest.setSessionId(sessionId);
                    brokerRequest.setMaxWidth(request.getMaxWidth());
                    brokerRequest.setMaxHeight(request.getMaxHeight());
                    brokerRequestList.add(brokerRequest);
                }
            }
            return mapper.mapToGetSessionScreenshotsUIResponse(getSessionsApi().getSessionScreenshots(brokerRequestList));
        } catch (ApiException e) {
            if (e.getCode() == 400 && (e.getMessage().contains("MaxWidth") || e.getMessage().contains("MaxHeight"))) {
                log.warn("Broker does not support maxWidth and maxHeight parameters for getSessionScreenshots");
                return getSessionScreenshotsWithBrokerConfig(request);
            }
            else if (e.getCode() == 400) {
                throw new BadRequestException(e);
            }
            throw new BrokerClientException(e);
        }
    }

    private GetSessionScreenshotsUIResponse getSessionScreenshotsWithBrokerConfig(GetSessionScreenshotsUIRequestData request) {
        try {
            List<GetSessionScreenshotRequestData> brokerRequestList = new ArrayList<>();
            if (request.getSessionIds() != null) {
                for (String sessionId : request.getSessionIds()) {
                    GetSessionScreenshotRequestData brokerRequest = new GetSessionScreenshotRequestData();
                    brokerRequest.setSessionId(sessionId);
                    brokerRequestList.add(brokerRequest);
                }
            }
            return mapper.mapToGetSessionScreenshotsUIResponse(getSessionsApi().getSessionScreenshots(brokerRequestList));
        } catch (ApiException e) {
            if (e.getCode() == 400) {
                throw new BadRequestException(e);
            }
            throw new BrokerClientException(e);
        }
    }

    private DescribeServersRequestData getDescribeServersBrokerRequest(DescribeServersUIRequestData request) {
        DescribeServersRequestData brokerRequest = new DescribeServersRequestData();
        brokerRequest.setMaxResults(request.getMaxResults());
        brokerRequest.setNextToken(request.getNextToken());

        // Add the server-id equals to the request since the broker supports it
        if(request.getIds() != null) {
            for (FilterToken token : request.getIds()) {
                if (FilterToken.OperatorEnum.EQUAL.equals(token.getOperator())) {
                    brokerRequest.addServerIdsItem(token.getValue());
                }
            }
        }
        return brokerRequest;
    }

    @Override
    public DescribeServersUIResponse describeServers(DescribeServersUIRequestData request) {
        DescribeServersRequestData brokerRequest = getDescribeServersBrokerRequest(request);
        return mapper.mapToDescribeServersUIResponse(describeServers(brokerRequest));
    }

    private DescribeServersResponse describeServers(DescribeServersRequestData brokerRequest) {
        try {
            ServersApi serversApi = serversApiProvider.getIfAvailable();
            serversApi.getApiClient().setBearerToken(tokenClient.getToken());

            return serversApi.describeServers(brokerRequest);
        } catch (ApiException e) {
            if (e.getCode() == 400) {
                throw new BadRequestException(e);
            }
            throw new BrokerClientException(e);
        }
    }

    @Override
    public void validateSessionTemplate(CreateSessionTemplateRequestData request, boolean ignoreExisting) {
        Map<String, String> errors = new HashMap<>();
        if (StringUtils.isBlank(request.getName())) {
            errors.put("Name", "Session template name required.");
        }
        if (!StringUtils.isBlank(request.getName()) && request.getName().length() > 255) {
            errors.put("Name", "Length should be at most 255 characters.");
        }
        if (!ignoreExisting && !StringUtils.isBlank(request.getName())) {
            if (sessionTemplateRepository.existsById(UUID.nameUUIDFromBytes(request.getName().getBytes()).toString())) {
                errors.put("Name", "A template with this name already exists.");
            }
        }

        if (request.getOsFamily() == null) {
            errors.put("OsFamily", "Operating system required.");
        }

        if (request.getDescription() != null && request.getDescription().length() > 1000) {
            errors.put("Description", "Length should be at most 1000 characters.");
        }

        if (request.getType() == null) {
            errors.put("Type", "Session type required.");
        }

        if (request.getOsFamily() == OsFamily.WINDOWS && request.getType() == Type.VIRTUAL) {
            errors.put("Type",
                    "Session type cannot be " + Type.VIRTUAL.getValue() + " for " + OsFamily.WINDOWS.getValue() + ".");
        }

        if(request.getOsFamily() == OsFamily.LINUX && request.getType() == Type.CONSOLE && request.getAutorunFile() != null) {
            errors.put("AutorunFile", "AutorunFile not supported for " + OsFamily.LINUX.getValue() + " " + Type.CONSOLE.getValue() + " type.");
        }

        if(request.getDcvGlEnabled() != null && request.getType() == Type.CONSOLE) {
            errors.put("DcvGlEnabled", "DcvGlEnabled not supported for " + Type.CONSOLE.getValue() + " type.");
        }

        if(request.getInitFile() != null && request.getType() == Type.CONSOLE) {
            errors.put("InitFile", "InitFile not supported for " + Type.CONSOLE.getValue() + " type.");
        }

        if(request.getAutorunFileArguments() != null && StringUtils.isBlank(request.getAutorunFile())) {
            errors.put("AutorunFileArguments", "AutorunFileArguments must be used alongside AutorunFile.");
        }

        if(request.getRequirements() != null) {
            try {
                JavaccRequirementsParserMatcher.parseAndMatch(request.getRequirements());
            }
            catch(BadRequestException e) {
                errors.put("Requirements", e.getMessage());
            }
        }

        if(!errors.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(errors);
                throw new BadRequestException(json);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public CreateSessionsUIResponse createSessions(List<Pair<CreateSessionUIRequestData, SessionTemplate>> requests) {
        try {
            List<broker.model.CreateSessionRequestData> brokerRequests = new ArrayList<>();
            CreateSessionsUIResponse response = new CreateSessionsUIResponse();
            for(Pair<CreateSessionUIRequestData, SessionTemplate> request: requests) {
                Map<String, String> errors = new HashMap<>();
                CreateSessionUIRequestData handlerRequest = request.getValue0();
                SessionTemplate sessionTemplate = request.getValue1();
                if(StringUtils.isBlank(handlerRequest.getName())) {
                    errors.put("Name", "Session Name required");
                }

                if(StringUtils.isBlank(handlerRequest.getOwner())) {
                    errors.put("Owner", "Session Owner required");
                }

                if(sessionTemplate == null) {
                    errors.put("Session Template", "Session Template Id " + handlerRequest.getSessionTemplateId() + " not found");
                }

                if(!errors.isEmpty()) {
                    UnsuccessfulCreateSessionUIRequestData unsuccessfulCreateSessionRequestData = new UnsuccessfulCreateSessionUIRequestData().failureReasons(errors);
                    handler.model.CreateSessionRequestData createSessionRequestData = new CreateSessionRequestData().name(handlerRequest.getName());
                    createSessionRequestData.setOwner(handlerRequest.getOwner());
                    response.addUnsuccessfulListItem(unsuccessfulCreateSessionRequestData.createSessionRequestData(createSessionRequestData));
                }
                else {
                    broker.model.CreateSessionRequestData brokerRequest = new broker.model.CreateSessionRequestData().name(handlerRequest.getName());
                    brokerRequest.setOwner(handlerRequest.getOwner());
                    brokerRequest.setType(sessionTemplate.getType());
                    brokerRequest.setInitFile(sessionTemplate.getInitFile());
                    brokerRequest.setMaxConcurrentClients(sessionTemplate.getMaxConcurrentClients());
                    brokerRequest.setDcvGlEnabled(sessionTemplate.getDcvGlEnabled());
                    brokerRequest.setPermissionsFile(sessionTemplate.getPermissionsFile());
                    brokerRequest.setEnqueueRequest(sessionTemplate.getEnqueueRequest());
                    brokerRequest.setAutorunFile(sessionTemplate.getAutorunFile());
                    brokerRequest.setAutorunFileArguments(sessionTemplate.getAutorunFileArguments());
                    brokerRequest.setDisableRetryOnFailure(sessionTemplate.getDisableRetryOnFailure());
                    brokerRequest.setRequirements(sessionTemplate.getRequirements());
                    brokerRequest.setStorageRoot(sessionTemplate.getStorageRoot());
                    brokerRequests.add(brokerRequest);
                }
            }

            if(!brokerRequests.isEmpty()) {
                CreateSessionsResponse brokerResponse = getSessionsApi().createSessions(brokerRequests);
                if(brokerResponse.getUnsuccessfulList() != null) {
                    for(UnsuccessfulCreateSessionRequestData unsuccessfulCreateSessionRequestData: brokerResponse.getUnsuccessfulList()) {
                        CreateSessionRequestData createSessionRequestData = mapper.mapToHandlerCreateSessionRequestData(unsuccessfulCreateSessionRequestData.getCreateSessionRequestData());
                        UnsuccessfulCreateSessionUIRequestData unsuccessfulCreateSessionUIRequestData = new UnsuccessfulCreateSessionUIRequestData().createSessionRequestData(createSessionRequestData);
                        Map<String, String> brokerError = new HashMap<>();
                        brokerError.put("Broker", unsuccessfulCreateSessionRequestData.getFailureReason());
                        response.addUnsuccessfulListItem(unsuccessfulCreateSessionUIRequestData.failureReasons(brokerError));
                    }
                }
                if(brokerResponse.getSuccessfulList() != null) {
                    for(Session session: brokerResponse.getSuccessfulList()) {
                        response.addSuccessfulListItem(mapper.mapToHandlerSession(session));
                    }
                }
                response.setRequestId(brokerResponse.getRequestId());
            }

            return response;
        }
        catch (ApiException e) {
            if(e.getCode() == 400) {
                throw new BadRequestException(e);
            }
            throw new BrokerClientException(e);
        }
    }

    @Override
    public GetSessionConnectionDataUIResponse getSessionConnectionData(String sessionId, String username) {
        try {
            GetSessionConnectionDataApi getSessionConnectionDataApi = getSessionConnectionDataApiProvider.getIfAvailable();
            getSessionConnectionDataApi.getApiClient().setBearerToken(tokenClient.getToken());
            return mapper.mapToGetSessionConnectionDataUIResponse(getSessionConnectionDataApi.getSessionConnectionData(sessionId, username));
        }
        catch (ApiException e) {
            if(e.getCode() == 400) {
                throw new BadRequestException(e);
            }
            throw new BrokerClientException(e);
        }
    }

    @Override
    public DeleteSessionsUIResponse deleteSessions(List<DeleteSessionUIRequestData> deleteSessionsUIRequestData) {
        try {
            List<DeleteSessionRequestData> brokerRequests = new ArrayList<>();
            List<UnsuccessfulDeleteSessionResponse> unsuccessfulResponseList = new ArrayList<>();
            for (DeleteSessionUIRequestData request : deleteSessionsUIRequestData) {
                Map<String, String> errors = new HashMap<>();
                if (StringUtils.isBlank(request.getSessionId())) {
                    errors.put("SessionId", "Session Id required");
                }
                if (StringUtils.isBlank(request.getOwner())) {
                    errors.put("Owner", "Owner required");
                }

                String sessionId = request.getSessionId();
                String owner = request.getOwner();

                // Create a list of unsuccessful responses for each request
                if(!errors.isEmpty()) {
                    UnsuccessfulDeleteSessionResponse deleteSessionUnsuccessfulResponse = new UnsuccessfulDeleteSessionResponse()
                            .failureReasons(errors)
                            .sessionId(sessionId);
                    unsuccessfulResponseList.add(deleteSessionUnsuccessfulResponse);
                } else {
                    DeleteSessionRequestData brokerRequest = new DeleteSessionRequestData()
                            .sessionId(sessionId)
                            .owner(owner)
                            .force(true);
                    brokerRequests.add(brokerRequest);
                }
            }

            if (brokerRequests.isEmpty()) {
                return new DeleteSessionsUIResponse().unsuccessfulList(unsuccessfulResponseList);
            }

            // Make the request and map it to the UI response
            DeleteSessionsResponse brokerResponse = getSessionsApi().deleteSessions(brokerRequests);
            DeleteSessionsUIResponse mappedResponse = mapper.mapToDeleteSessionsDataUIResponse(brokerResponse);

            if (brokerResponse.getUnsuccessfulList() != null ) {
                // We need to erase the unsuccessful list, so we can transform the error reasons into a map
                mappedResponse.setUnsuccessfulList(new ArrayList<>());
                for (DeleteSessionUnsuccessfulResponse deleteSessionUnsuccessfulResponse : brokerResponse.getUnsuccessfulList()) {
                    HashMap<String, String> failureReasons = new HashMap<>();
                    failureReasons.put("Broker", deleteSessionUnsuccessfulResponse.getFailureReason());
                    UnsuccessfulDeleteSessionResponse unsuccessfulDeleteSessionResponse = new UnsuccessfulDeleteSessionResponse()
                            .sessionId(deleteSessionUnsuccessfulResponse.getSessionId())
                            .failureReasons(failureReasons);
                    mappedResponse.addUnsuccessfulListItem(unsuccessfulDeleteSessionResponse);
                }
            }

            // Add all the unsuccessful responses to the mapped response
            unsuccessfulResponseList.forEach(mappedResponse::addUnsuccessfulListItem);
            return mappedResponse;
        } catch (ApiException e) {
            if(e.getCode() == 400) {
                throw new BadRequestException(e);
            }
            throw new BrokerClientException(e);
        }
    }
}
