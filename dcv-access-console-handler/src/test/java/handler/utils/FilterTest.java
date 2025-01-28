// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import handler.model.FilterBooleanToken;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import handler.exceptions.BadRequestException;
import handler.model.Availability;
import handler.model.DescribeServersUIRequestData;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.Endpoint;
import handler.model.FilterAvailabilityToken;
import handler.model.FilterDateToken;
import handler.model.FilterNumberToken;
import handler.model.FilterOsFamilyToken;
import handler.model.FilterProtocolToken;
import handler.model.FilterStateToken;
import handler.model.FilterToken;
import handler.model.FilterTypeToken;
import handler.model.FilterUnavailabilityReasonToken;
import handler.model.GetSessionScreenshotsUIRequestData;
import handler.model.Host;
import handler.model.KeyValuePair;
import handler.model.Os;
import handler.model.OsFamily;
import handler.model.Protocol;
import handler.model.Server;
import handler.model.Session;
import handler.model.State;
import handler.model.Type;
import handler.model.UnavailabilityReason;
import handler.model.SessionTemplate;

@ExtendWith(MockitoExtension.class)
public class FilterTest {
    @InjectMocks
    private Filter<DescribeSessionsUIRequestData, Session> testFilter;
    @InjectMocks
    private Filter<DescribeServersUIRequestData, Server> testServerFilter;
    @InjectMocks
    private Filter<DescribeSessionTemplatesRequestData, SessionTemplate> testSessionTemplateFilter;
    @InjectMocks
    private Filter<GetSessionScreenshotsUIRequestData, Session> badTestFilter;
    private static List<Session> unfilteredSessions;
    private static List<Session> firstFilteredSession;
    private static List<Session> secondFilteredSession;
    private static List<Server> unfilteredServers;
    private static List<Server> filteredServer;
    private static List<SessionTemplate> unfilteredSessionTemplates;
    private static List<SessionTemplate> filteredSessionTemplate;
    private static DescribeSessionsUIRequestData request;
    private static DescribeServersUIRequestData serverRequest;
    private static DescribeSessionTemplatesRequestData sessionTemplateRequest;
    private final static String testString = "test";

    public FilterTest() {
        Server server1 = new Server().addEndpointsItem(new Endpoint().protocol(null));
        server1.addEndpointsItem(new Endpoint().protocol(Protocol.HTTP.toString()));
        server1.setAvailability(Availability.AVAILABLE.toString());
        server1.setUnavailabilityReason(UnavailabilityReason.UNKNOWN.toString());
        server1.setHost(new Host().os(new Os().family(OsFamily.LINUX.toString())));
        server1.addTagsItem(null);
        KeyValuePair tag = new KeyValuePair().key(testString);
        server1.addTagsItem(tag.value(testString));
        server1.setHostname(testString);
        Session session1 = new Session().name("session1");
        session1.setType("VIRTUAL");
        session1.setState("READY");
        session1.setNumOfConnections(1L);
        session1.setCreationTime(OffsetDateTime.parse("2023-07-10T00:00:00.001Z"));
        session1.setServer(server1);
        Session session2 = new Session().name("session2");
        session2.setType("CONSOLE");
        session2.setState("DELETED");
        session2.setNumOfConnections(2L);
        session2.setCreationTime(OffsetDateTime.parse("2023-07-10T00:00:00.002Z"));
        Session session3 = new Session();

        unfilteredSessions = new ArrayList<>();
        unfilteredSessions.add(session1);
        unfilteredSessions.add(session2);
        unfilteredSessions.add(session3);

        unfilteredServers = new ArrayList<>();
        unfilteredServers.add(server1);

        filteredServer = new ArrayList<>();
        filteredServer.add(server1);

        firstFilteredSession = new ArrayList<>();
        firstFilteredSession.add(session1);

        secondFilteredSession = new ArrayList<>();
        secondFilteredSession.add(session2);

        SessionTemplate sessionTemplate = new SessionTemplate().dcvGlEnabled(true);
        filteredSessionTemplate = new ArrayList<>();
        filteredSessionTemplate.add(sessionTemplate);
        unfilteredSessionTemplates = new ArrayList<>();
        unfilteredSessionTemplates.add(sessionTemplate);

        request = new DescribeSessionsUIRequestData();
        serverRequest = new DescribeServersUIRequestData();
        sessionTemplateRequest = new DescribeSessionTemplatesRequestData();
    }

    @Test
    public void testBadRequestException() {
        assertThrowsExactly(BadRequestException.class,
                () -> testFilter.getFiltered(null, unfilteredSessions));

        assertThrowsExactly(BadRequestException.class,
                () -> testFilter.getFiltered(request.addSessionNamesItem(null), unfilteredSessions));

        FilterToken filterToken = new FilterToken();
        request.setSessionNames(null);
        assertThrowsExactly(BadRequestException.class,
                () -> testFilter.getFiltered(request.addSessionNamesItem(filterToken.operator(null)), unfilteredSessions));


        request.setCreationTimes(null);
        assertThrowsExactly(BadRequestException.class,
                () -> testFilter.getFiltered(request.addSessionNamesItem(filterToken.operator(FilterToken.OperatorEnum.EQUAL)), null));

        assertThrowsExactly(BadRequestException.class,
                () -> testFilter.getFiltered(request.addSessionNamesItem(filterToken.value(null)), unfilteredSessions));

        request.setSessionNames(null);
        assertThrowsExactly(BadRequestException.class,
                () -> testFilter.getFiltered(request.addSessionNamesItem(filterToken.value(null)), unfilteredSessions));

        assertThrowsExactly(BadRequestException.class,
                () -> badTestFilter.getFiltered(new GetSessionScreenshotsUIRequestData(), null));

    }

    @Test
    public void testNullOperatorValuesForAutoComplete() {
        List<Session> filtered;
        List<Server> filteredServers;
        List<Session> expectedSessions = new ArrayList<>();

        expectedSessions.addAll(firstFilteredSession);
        expectedSessions.addAll(secondFilteredSession);

        request.setSessionNames(null);
        filtered = testFilter.getFiltered(request.addTypesItem(new FilterTypeToken().operator(null)),
                unfilteredSessions);
        assertEquals(expectedSessions, filtered);

        request.setStates(null);
        filtered = testFilter.getFiltered(request.addStatesItem(new FilterStateToken().operator(null)),
                unfilteredSessions);
        assertEquals(expectedSessions, filtered);

        request.setNumOfConnections(null);
        filtered = testFilter.getFiltered(request.addNumOfConnectionsItem(new FilterNumberToken().operator(null)),
                unfilteredSessions);
        assertEquals(expectedSessions, filtered);

        filtered = testFilter.getFiltered(request.addCreationTimesItem(new FilterDateToken().operator(null).value("2023-07-10T00:00:00.000Z")),
                unfilteredSessions);
        assertEquals(expectedSessions, filtered);

        serverRequest.setProtocols(null);
        filteredServers = testServerFilter.getFiltered(serverRequest.addProtocolsItem(new FilterProtocolToken().operator(null)),
                unfilteredServers);
        assertEquals(unfilteredServers, filteredServers);

        serverRequest.setAvailabilities(null);
        filteredServers = testServerFilter.getFiltered(serverRequest.addAvailabilitiesItem(new FilterAvailabilityToken().operator(null)),
                unfilteredServers);
        assertEquals(unfilteredServers, filteredServers);

        serverRequest.setUnavailabilityReasons(null);
        filteredServers = testServerFilter.getFiltered(serverRequest.addUnavailabilityReasonsItem(new FilterUnavailabilityReasonToken().operator(null)),
                unfilteredServers);
        assertEquals(unfilteredServers, filteredServers);

        serverRequest.setOsFamilies(null);
        filteredServers = testServerFilter.getFiltered(serverRequest.addOsFamiliesItem(new FilterOsFamilyToken().operator(null)),
                unfilteredServers);
        assertEquals(unfilteredServers, filteredServers);

        sessionTemplateRequest.setDcvGlEnabled(null);
        List<SessionTemplate> filteredSessionTemplates = testSessionTemplateFilter.getFiltered(sessionTemplateRequest.addDcvGlEnabledItem(new FilterBooleanToken().operator(null)), unfilteredSessionTemplates);
        assertEquals(unfilteredSessionTemplates, filteredSessionTemplates);
    }

    @Test
    public void testGetFilteredSuccess() {
        FilterToken filterToken = new FilterToken().operator(FilterToken.OperatorEnum.EQUAL);
        filterToken.setValue(testString);
        request.addSessionNamesItem(filterToken);
        filterToken = new FilterToken().operator(FilterToken.OperatorEnum.CONTAINS);
        filterToken.setValue(testString);
        request.addSessionNamesItem(filterToken);
        filterToken = new FilterToken().operator(FilterToken.OperatorEnum.NOT_EQUAL);
        filterToken.setValue("session2");
        request.addSessionNamesItem(filterToken);
        List<Session> filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setSessionNames(null);
        filterToken.setOperator(FilterToken.OperatorEnum.NOT_CONTAINS);
        request.addSessionNamesItem(filterToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setSessionNames(null);
        FilterTypeToken filterTypeToken = new FilterTypeToken().operator(FilterTypeToken.OperatorEnum.EQUAL);
        filterTypeToken.setValue(Type.VIRTUAL);
        request.addTypesItem(filterTypeToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setTypes(null);
        filterTypeToken.setOperator(FilterTypeToken.OperatorEnum.NOT_EQUAL);
        filterTypeToken.setValue(Type.CONSOLE);
        request.addTypesItem(filterTypeToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setTypes(null);
        FilterStateToken filterStateToken = new FilterStateToken().operator(FilterStateToken.OperatorEnum.EQUAL);
        filterStateToken.setValue(State.CREATING);
        request.addStatesItem(filterStateToken);
        filterStateToken = new FilterStateToken().operator(FilterStateToken.OperatorEnum.NOT_EQUAL);
        filterStateToken.setValue(State.DELETED);
        request.addStatesItem(filterStateToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setStates(null);
        FilterNumberToken filterNumberToken = new FilterNumberToken().operator(FilterNumberToken.OperatorEnum.EQUAL);
        filterNumberToken.setValue(3L);
        request.addNumOfConnectionsItem(filterNumberToken);
        filterNumberToken = new FilterNumberToken().operator(FilterNumberToken.OperatorEnum.NOT_EQUAL);
        filterNumberToken.setValue(2L);
        request.addNumOfConnectionsItem(filterNumberToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setNumOfConnections(null);
        filterNumberToken.setOperator(FilterNumberToken.OperatorEnum.LESS_THAN);
        request.addNumOfConnectionsItem(filterNumberToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setNumOfConnections(null);
        filterNumberToken.setOperator(FilterNumberToken.OperatorEnum.LESS_THAN_OR_EQUAL_TO);
        filterNumberToken.setValue(1L);
        request.addNumOfConnectionsItem(filterNumberToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setNumOfConnections(null);
        filterNumberToken.setOperator(FilterNumberToken.OperatorEnum.GREATER_THAN);
        request.addNumOfConnectionsItem(filterNumberToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(secondFilteredSession, filtered);

        request.setNumOfConnections(null);
        filterNumberToken.setOperator(FilterNumberToken.OperatorEnum.GREATER_THAN_OR_EQUAL_TO);
        filterNumberToken.setValue(2L);
        request.addNumOfConnectionsItem(filterNumberToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(secondFilteredSession, filtered);

        request.setNumOfConnections(null);
        FilterDateToken filterDateToken = new FilterDateToken().operator(FilterDateToken.OperatorEnum.EQUAL);
        filterDateToken.setValue("2023-07-10T00:00:00.003Z");
        request.addCreationTimesItem(filterDateToken);
        filterDateToken = new FilterDateToken().operator(FilterDateToken.OperatorEnum.NOT_EQUAL);
        filterDateToken.setValue("2023-07-10T00:00:00.002Z");
        request.addCreationTimesItem(filterDateToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setCreationTimes(null);
        filterDateToken.setOperator(FilterDateToken.OperatorEnum.LESS_THAN);
        request.addCreationTimesItem(filterDateToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setCreationTimes(null);
        filterDateToken.setOperator(FilterDateToken.OperatorEnum.LESS_THAN_OR_EQUAL_TO);
        filterDateToken.setValue("2023-07-10T00:00:00.001Z");
        request.addCreationTimesItem(filterDateToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);

        request.setCreationTimes(null);
        filterDateToken.setOperator(FilterDateToken.OperatorEnum.GREATER_THAN);
        request.addCreationTimesItem(filterDateToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(secondFilteredSession, filtered);

        request.setCreationTimes(null);
        filterDateToken.setOperator(FilterDateToken.OperatorEnum.GREATER_THAN_OR_EQUAL_TO);
        filterDateToken.setValue("2023-07-10T00:00:00.002Z");
        request.addCreationTimesItem(filterDateToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(secondFilteredSession, filtered);

        request.setCreationTimes(null);
        filterToken.setOperator(FilterToken.OperatorEnum.EQUAL);
        filterToken.setValue(testString);
        request.addHostnamesItem(filterToken);
        filtered = testFilter.getFiltered(request, unfilteredSessions);
        assertEquals(firstFilteredSession, filtered);
        request.setHostnames(null);

        FilterProtocolToken filterProtocolToken = new FilterProtocolToken().operator(FilterProtocolToken.OperatorEnum.EQUAL);
        filterProtocolToken.setValue(Protocol.QUIC);
        serverRequest.addProtocolsItem(filterProtocolToken);
        filterProtocolToken = new FilterProtocolToken().operator(FilterProtocolToken.OperatorEnum.NOT_EQUAL);
        filterProtocolToken.setValue(Protocol.QUIC);
        serverRequest.addProtocolsItem(filterProtocolToken);
        FilterAvailabilityToken filterAvailabilityToken = new FilterAvailabilityToken().operator(FilterAvailabilityToken.OperatorEnum.EQUAL);
        filterAvailabilityToken.setValue(Availability.UNAVAILABLE);
        serverRequest.addAvailabilitiesItem(filterAvailabilityToken);
        filterAvailabilityToken = new FilterAvailabilityToken().operator(FilterAvailabilityToken.OperatorEnum.NOT_EQUAL);
        filterAvailabilityToken.setValue(Availability.UNAVAILABLE);
        serverRequest.addAvailabilitiesItem(filterAvailabilityToken);
        FilterUnavailabilityReasonToken filterUnavailabilityReasonToken = new FilterUnavailabilityReasonToken().operator(FilterUnavailabilityReasonToken.OperatorEnum.EQUAL);
        filterUnavailabilityReasonToken.setValue(UnavailabilityReason.SERVER_FULL);
        serverRequest.addUnavailabilityReasonsItem(filterUnavailabilityReasonToken);
        filterUnavailabilityReasonToken = new FilterUnavailabilityReasonToken().operator(FilterUnavailabilityReasonToken.OperatorEnum.NOT_EQUAL);
        filterUnavailabilityReasonToken.setValue(UnavailabilityReason.SERVER_FULL);
        serverRequest.addUnavailabilityReasonsItem(filterUnavailabilityReasonToken);
        FilterOsFamilyToken filterOsFamilyToken = new FilterOsFamilyToken().operator(FilterOsFamilyToken.OperatorEnum.EQUAL);
        filterOsFamilyToken.setValue(OsFamily.WINDOWS);
        serverRequest.addOsFamiliesItem(filterOsFamilyToken);
        filterOsFamilyToken = new FilterOsFamilyToken().operator(FilterOsFamilyToken.OperatorEnum.NOT_EQUAL);
        filterOsFamilyToken.setValue(OsFamily.WINDOWS);
        serverRequest.addOsFamiliesItem(filterOsFamilyToken);
        serverRequest.addTagsItem(new KeyValuePair());
        KeyValuePair tag = new KeyValuePair().key(testString);
        tag.setValue(testString);
        serverRequest.addTagsItem(tag);
        List<Server> filteredServers = testServerFilter.getFiltered(serverRequest, unfilteredServers);
        assertEquals(filteredServer, filteredServers);

        serverRequest.setProtocols(null);
        serverRequest.setAvailabilities(null);
        serverRequest.setUnavailabilityReasons(null);
        serverRequest.setOsFamilies(null);
        serverRequest.setTags(null);
        filterProtocolToken.setOperator(FilterProtocolToken.OperatorEnum.NOT_EQUAL);
        filterProtocolToken.setValue(Protocol.HTTP);
        serverRequest.addProtocolsItem(filterProtocolToken);
        filteredServers = testServerFilter.getFiltered(serverRequest, unfilteredServers);
        assertEquals(0, filteredServers.size());

        serverRequest.setProtocols(null);
        filterAvailabilityToken.setOperator(FilterAvailabilityToken.OperatorEnum.NOT_EQUAL);
        filterAvailabilityToken.setValue(Availability.AVAILABLE);
        serverRequest.addAvailabilitiesItem(filterAvailabilityToken);
        filteredServers = testServerFilter.getFiltered(serverRequest, unfilteredServers);
        assertEquals(0, filteredServers.size());

        serverRequest.setAvailabilities(null);
        filterUnavailabilityReasonToken.setOperator(FilterUnavailabilityReasonToken.OperatorEnum.NOT_EQUAL);
        filterUnavailabilityReasonToken.setValue(UnavailabilityReason.UNKNOWN);
        serverRequest.addUnavailabilityReasonsItem(filterUnavailabilityReasonToken);
        filteredServers = testServerFilter.getFiltered(serverRequest, unfilteredServers);
        assertEquals(0, filteredServers.size());

        serverRequest.setUnavailabilityReasons(null);
        filterOsFamilyToken.setOperator(FilterOsFamilyToken.OperatorEnum.NOT_EQUAL);
        filterOsFamilyToken.setValue(OsFamily.LINUX);
        serverRequest.addOsFamiliesItem(filterOsFamilyToken);
        filteredServers = testServerFilter.getFiltered(serverRequest, unfilteredServers);
        assertEquals(0, filteredServers.size());

        FilterBooleanToken filterBooleanToken = new FilterBooleanToken().operator(FilterBooleanToken.OperatorEnum.EQUAL).value(true);
        List<SessionTemplate> filteredSessionTemplates = testSessionTemplateFilter.getFiltered(sessionTemplateRequest.addDcvGlEnabledItem(filterBooleanToken), unfilteredSessionTemplates);
        assertEquals(filteredSessionTemplate, filteredSessionTemplates);

        sessionTemplateRequest.setDcvGlEnabled(null);
        filterBooleanToken = new FilterBooleanToken().operator(FilterBooleanToken.OperatorEnum.NOT_EQUAL).value(true);
        sessionTemplateRequest.addDcvGlEnabledItem(filterBooleanToken);
        filterBooleanToken = new FilterBooleanToken().operator(FilterBooleanToken.OperatorEnum.NOT_EQUAL).value(false);
        filteredSessionTemplates = testSessionTemplateFilter.getFiltered(sessionTemplateRequest.addDcvGlEnabledItem(filterBooleanToken), unfilteredSessionTemplates);
        assertEquals(filteredSessionTemplate, filteredSessionTemplates);
    }
}
