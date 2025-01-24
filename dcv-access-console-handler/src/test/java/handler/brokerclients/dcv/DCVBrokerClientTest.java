package handler.brokerclients.dcv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import broker.api.GetSessionConnectionDataApi;
import broker.model.*;
import broker.model.Aws;
import broker.model.CpuInfo;
import broker.model.CpuLoadAverage;
import broker.model.CreateSessionRequestData;
import broker.model.Endpoint;
import broker.model.GetSessionScreenshotRequestData;
import broker.model.GetSessionScreenshotSuccessfulResponse;
import broker.model.GetSessionScreenshotUnsuccessfulResponse;
import broker.model.Gpu;
import broker.model.Host;
import broker.model.LoggedInUser;
import broker.model.Memory;
import broker.model.Os;
import broker.model.Server;
import broker.model.Session;
import broker.model.SessionScreenshot;
import broker.model.SessionScreenshotImage;
import broker.model.Swap;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.model.KeyValuePair;
import handler.repositories.PagingAndSortingCrudRepository;
import org.javatuples.Pair;

import java.nio.charset.StandardCharsets;
import java.util.*;

import handler.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import broker.ApiClient;
import broker.ApiException;
import broker.api.ServersApi;
import broker.api.SessionsApi;
import handler.exceptions.BadRequestException;
import handler.exceptions.BrokerClientException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class DCVBrokerClientTest {
    private SessionsApi mockSessionsApi;
    private ServersApi mockServersApi;
    private GetSessionConnectionDataApi mockGetSessionConnectionDataApi;
    private PagingAndSortingCrudRepository mockSessionTemplateRepository;
    private ObjectMapper mockObjectMapper;
    private DCVBrokerClient testBrokerClient;
    private final static String testString = "test";
    private final static String userString = "test-user";
    private final static String idString = "test-id";
    private final static String osString = "test-os";
    private final static String cpuVendor = "test-vendor";
    private final static String serverId = "test-server";
    private final static State TEST_STATE = State.DELETING;
    private final static Long maxWidth = 3840L;
    private final static Long maxHeight = 2160L;

    public DCVBrokerClientTest() {
        ObjectProvider<SessionsApi> mockSessionsApiProvider = mock(ObjectProvider.class);
        ObjectProvider<ServersApi> mockServersApiProvider = mock(ObjectProvider.class);
        ObjectProvider<GetSessionConnectionDataApi> mockGetSessionConnectionDataApiProvider = mock(ObjectProvider.class);
        mockSessionsApi = mock(SessionsApi.class);
        mockServersApi = mock(ServersApi.class);
        mockGetSessionConnectionDataApi = mock(GetSessionConnectionDataApi.class);
        mockSessionTemplateRepository = mock(PagingAndSortingCrudRepository.class);
        mockObjectMapper = mock(ObjectMapper.class);
        when(mockSessionsApiProvider.getIfAvailable()).thenReturn(mockSessionsApi);
        when(mockSessionsApi.getApiClient()).thenReturn(new ApiClient());
        when(mockServersApiProvider.getIfAvailable()).thenReturn(mockServersApi);
        when(mockServersApi.getApiClient()).thenReturn(new ApiClient());
        when(mockGetSessionConnectionDataApiProvider.getIfAvailable()).thenReturn(mockGetSessionConnectionDataApi);
        when(mockGetSessionConnectionDataApi.getApiClient()).thenReturn(new ApiClient());
        testBrokerClient = new DCVBrokerClient(mockSessionsApiProvider, mockServersApiProvider, mockGetSessionConnectionDataApiProvider, new DCVBrokerHandlerMapperImpl(), mock(DCVBrokerTokenClient.class), mockObjectMapper, mockSessionTemplateRepository);
    }

    @Test
    public void testDescribeSessionsBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> {
                    doThrow(new ApiException(400, "Error code 400")).when(mockSessionsApi).describeSessions(any());
                    testBrokerClient.describeSessions(new DescribeSessionsUIRequestData());
                });
    }

    @Test
    public void testDescribeSessionsBrokerClientException() {
        assertThrowsExactly(BrokerClientException.class,
                () -> {
                    doThrow(ApiException.class).when(mockSessionsApi).describeSessions(any());
                    testBrokerClient.describeSessions(new DescribeSessionsUIRequestData());
                });
    }

    @Test
    public void testGetSessionScreenshotsBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> {
                    doThrow(new ApiException(400, "Error code 400")).when(mockSessionsApi).getSessionScreenshots(any());
                    testBrokerClient.getSessionScreenshots(new GetSessionScreenshotsUIRequestData().addSessionIdsItem(idString).maxWidth(maxWidth).maxHeight(maxHeight));
                });
    }

    @Test
    public void testGetSessionScreenshotsBrokerClientException() {
        assertThrowsExactly(BrokerClientException.class,
                () -> {
                    doThrow(ApiException.class).when(mockSessionsApi).getSessionScreenshots(any());
                    testBrokerClient.getSessionScreenshots(new GetSessionScreenshotsUIRequestData().addSessionIdsItem(idString).maxWidth(maxWidth).maxHeight(maxHeight));
                });
    }

    @Test
    public void testGetSessionScreenshotsRetryOnUnsupportedParameters() throws ApiException {
        GetSessionScreenshotsUIRequestData handlerRequest = new GetSessionScreenshotsUIRequestData()
                .addSessionIdsItem(testString)
                .maxWidth(maxWidth)
                .maxHeight(maxHeight);

        ApiException unsupportedParamException = new ApiException(400, "Unrecognized field \"MaxWidth\"");
        GetSessionScreenshotsResponse brokerResponse = new GetSessionScreenshotsResponse().addSuccessfulListItem(new GetSessionScreenshotSuccessfulResponse().sessionScreenshot(new SessionScreenshot().addImagesItem(new SessionScreenshotImage().data(testString))));

        ArgumentCaptor<List<GetSessionScreenshotRequestData>> requestCaptor = ArgumentCaptor.forClass(List.class);
        when(mockSessionsApi.getSessionScreenshots(requestCaptor.capture()))
                .thenThrow(unsupportedParamException)
                .thenReturn(brokerResponse);

        GetSessionScreenshotsUIResponse response = testBrokerClient.getSessionScreenshots(handlerRequest);

        verify(mockSessionsApi, times(2)).getSessionScreenshots(requestCaptor.capture());

        List<List<GetSessionScreenshotRequestData>> capturedRequests = requestCaptor.getAllValues();
        List<GetSessionScreenshotRequestData> firstRequest = capturedRequests.get(0);
        assertFalse(firstRequest.isEmpty());
        GetSessionScreenshotRequestData firstRequestData = firstRequest.get(0);
        assertEquals(testString, firstRequestData.getSessionId());
        assertEquals(maxWidth, firstRequestData.getMaxWidth());
        assertEquals(maxHeight, firstRequestData.getMaxHeight());

        List<GetSessionScreenshotRequestData> secondRequest = capturedRequests.get(1);
        assertFalse(secondRequest.isEmpty());
        GetSessionScreenshotRequestData secondRequestData = secondRequest.get(0);
        assertNull(secondRequestData.getMaxWidth());
        assertNull(secondRequestData.getMaxHeight());

        assertEquals(1, response.getSuccessfulList().size());
        assertEquals(testString, response.getSuccessfulList().get(0).getSessionScreenshot().getImages().get(0).getData());
        assertNull(response.getUnsuccessfulList());
        assertNull(response.getError());
    }

    @Test
    public void testGetSessionScreenshotsUnsupportedParametersToBadRequest() throws ApiException{
        GetSessionScreenshotsUIRequestData handlerRequest = new GetSessionScreenshotsUIRequestData()
                .addSessionIdsItem(testString)
                .maxWidth(maxWidth)
                .maxHeight(maxHeight);

        ApiException unsupportedParamException = new ApiException(400, "Unrecognized field \"MaxWidth\"");
        ApiException badRequestException = new ApiException(400, "Bad Request");

        ArgumentCaptor<List<GetSessionScreenshotRequestData>> requestCaptor = ArgumentCaptor.forClass(List.class);
        when(mockSessionsApi.getSessionScreenshots(requestCaptor.capture()))
                .thenThrow(unsupportedParamException)
                .thenThrow(badRequestException);
        assertThrows(BadRequestException.class, () -> testBrokerClient.getSessionScreenshots(handlerRequest));

        List<List<GetSessionScreenshotRequestData>> capturedRequests = requestCaptor.getAllValues();
        List<GetSessionScreenshotRequestData> firstRequest = capturedRequests.get(0);
        assertFalse(firstRequest.isEmpty());
        GetSessionScreenshotRequestData firstRequestData = firstRequest.get(0);
        assertEquals(testString, firstRequestData.getSessionId());
        assertEquals(maxWidth, firstRequestData.getMaxWidth());
        assertEquals(maxHeight, firstRequestData.getMaxHeight());

        List<GetSessionScreenshotRequestData> secondRequest = capturedRequests.get(1);
        assertFalse(secondRequest.isEmpty());
        GetSessionScreenshotRequestData secondRequestData = secondRequest.get(0);
        assertEquals(testString, secondRequestData.getSessionId());
        assertNull(secondRequestData.getMaxWidth());
        assertNull(secondRequestData.getMaxHeight());
    }

    @Test
    public void testGetSessionScreenshotsUnsupportedParametersToBrokerClientException() throws ApiException {
        GetSessionScreenshotsUIRequestData handlerRequest = new GetSessionScreenshotsUIRequestData()
                .addSessionIdsItem(testString)
                .maxWidth(maxWidth)
                .maxHeight(maxHeight);

        ApiException unsupportedParamException = new ApiException(400, "Unrecognized field \"MaxWidth\"");
        ApiException brokerClientException = new ApiException(500, "Broker Client Exception");

        ArgumentCaptor<List<GetSessionScreenshotRequestData>> requestCaptor = ArgumentCaptor.forClass(List.class);
        when(mockSessionsApi.getSessionScreenshots(requestCaptor.capture()))
                .thenThrow(unsupportedParamException)
                .thenThrow(brokerClientException);
        assertThrows(BrokerClientException.class, () -> testBrokerClient.getSessionScreenshots(handlerRequest));

        List<List<GetSessionScreenshotRequestData>> capturedRequests = requestCaptor.getAllValues();
        List<GetSessionScreenshotRequestData> firstRequest = capturedRequests.get(0);
        assertFalse(firstRequest.isEmpty());
        GetSessionScreenshotRequestData firstRequestData = firstRequest.get(0);
        assertEquals(testString, firstRequestData.getSessionId());
        assertEquals(maxWidth, firstRequestData.getMaxWidth());
        assertEquals(maxHeight, firstRequestData.getMaxHeight());

        List<GetSessionScreenshotRequestData> secondRequest = capturedRequests.get(1);
        assertFalse(secondRequest.isEmpty());
        GetSessionScreenshotRequestData secondRequestData = secondRequest.get(0);
        assertEquals(testString, secondRequestData.getSessionId());
        assertNull(secondRequestData.getMaxWidth());
        assertNull(secondRequestData.getMaxHeight());
    }
    @Test
    public void testDescribeServersBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> {
                    doThrow(new ApiException(400, "Error code 400")).when(mockServersApi).describeServers(any());
                    testBrokerClient.describeServers(new DescribeServersUIRequestData());
                });
    }

    @Test
    public void testDescribeServersBrokerClientException() {
        assertThrowsExactly(BrokerClientException.class,
                () -> {
                    doThrow(ApiException.class).when(mockServersApi).describeServers(any());
                    testBrokerClient.describeServers(new DescribeServersUIRequestData());
                });
    }

    @Test
    public void testCreateSessionsBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> {
                    doThrow(new ApiException(400, "Error code 400")).when(mockSessionsApi).createSessions(any());
                    CreateSessionUIRequestData request = new CreateSessionUIRequestData().name(testString);
                    testBrokerClient.createSessions(List.of(new Pair<>(request.owner(testString), new SessionTemplate().id(idString))));
                });
    }

    @Test
    public void testCreateSessionsBrokerClientException() {
        assertThrowsExactly(BrokerClientException.class,
                () -> {
                    doThrow(ApiException.class).when(mockSessionsApi).createSessions(any());
                    CreateSessionUIRequestData request = new CreateSessionUIRequestData().name(testString);
                    testBrokerClient.createSessions(List.of(new Pair<>(request.owner(testString).sessionTemplateId(idString), new SessionTemplate().id(idString))));
                });
    }

    @Test
    public void testGetSessionConnectionDataBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> {
                    doThrow(new ApiException(400, "Error code 400")).when(mockGetSessionConnectionDataApi).getSessionConnectionData(any(), any());
                    testBrokerClient.getSessionConnectionData(testString, testString);
                });
    }

    @Test
    public void testGetSessionConnectionDataBrokerClientException() {
        assertThrowsExactly(BrokerClientException.class,
                () -> {
                    doThrow(ApiException.class).when(mockGetSessionConnectionDataApi).getSessionConnectionData(any(), any());
                    testBrokerClient.getSessionConnectionData(testString, testString);
                });
    }

    @Test
    public void testDescribeServersSuccess() throws Exception {
        DescribeServersResponse brokerResponse = new DescribeServersResponse();
        Server server = new Server().id(testString);
        FilterToken token = new FilterToken().operator(FilterToken.OperatorEnum.EQUAL).value(testString);
        DescribeServersUIRequestData handlerRequest = new DescribeServersUIRequestData().addIdsItem(token);

        doReturn(brokerResponse.addServersItem(server)).when(mockServersApi).describeServers(any());
        DescribeServersUIResponse response = testBrokerClient.describeServers(handlerRequest);
        assertEquals(1, response.getServers().size());
        assertNull(response.getError());

        brokerResponse.setServers(null);
        token.setOperator(FilterToken.OperatorEnum.NOT_EQUAL);
        response = testBrokerClient.describeServers(handlerRequest);
        assertNull(response.getServers());
        assertNull(response.getError());

        doReturn(null).when(mockServersApi).describeServers(any());
        response = testBrokerClient.describeServers(handlerRequest);
        assertNull(response);
    }

    @Test
    public void testDescribeSessionsSuccess() throws Exception {
        Session session = new Session().name(testString);
        FilterToken token = new FilterToken().operator(FilterToken.OperatorEnum.EQUAL).value(testString);
        DescribeSessionsUIRequestData handlerRequest = new DescribeSessionsUIRequestData().addSessionIdsItem(token);
        DescribeSessionsResponse brokerResponse = new DescribeSessionsResponse();

        Server sessionServer = new Server().id(serverId);
        Server fullServer = new Server().id(serverId);

        Host host = new Host();

        host.setOs(new Os().name(osString));
        host.setMemory(new Memory().totalBytes(0L));
        host.setSwap(new Swap().totalBytes(0L));
        host.setAws(new Aws().region(testString));
        host.setCpuInfo(new CpuInfo().vendor(cpuVendor));
        host.setCpuLoadAverage(new CpuLoadAverage().oneMinute(0F));
        host.addGpusItem(new Gpu().vendor(testString));
        host.addLoggedInUsersItem(new LoggedInUser().username(testString));

        sessionServer.addEndpointsItem(new Endpoint().protocol(Protocol.HTTP.toString()));
        sessionServer.addTagsItem(new broker.model.KeyValuePair());
        sessionServer.setAvailability(Availability.AVAILABLE.toString());

        session.server(sessionServer);

        fullServer.addEndpointsItem(new Endpoint().protocol(Protocol.HTTP.toString()));
        fullServer.addTagsItem(new broker.model.KeyValuePair());
        fullServer.setAvailability(Availability.AVAILABLE.toString());
        fullServer.unavailabilityReason(UnavailabilityReason.UNKNOWN.toString());
        fullServer.setHost(host);

        brokerResponse.addSessionsItem(session);
        doReturn(brokerResponse).when(mockSessionsApi).describeSessions(any());

        DescribeServersResponse describeServersResponse = new DescribeServersResponse();
        describeServersResponse.addServersItem(fullServer);

        doAnswer(invocation -> {
            DescribeServersRequestData requestData = invocation.getArgument(0);
            if (requestData.getServerIds().size() == 1) {
                assertEquals(serverId, requestData.getServerIds().get(0));
                return describeServersResponse;
            }

            return new DescribeServersResponse().servers(Collections.emptyList());
        }).when(mockServersApi).describeServers(any());

        DescribeSessionsUIResponse response = testBrokerClient.describeSessions(handlerRequest);

        assertEquals(1, response.getSessions().size());
        assertEquals(testString, response.getSessions().get(0).getName());
        assertEquals(osString, response.getSessions().get(0).getServer().getHost().getOs().getName());
        assertEquals(0L, response.getSessions().get(0).getServer().getHost().getMemory().getTotalBytes());
        assertEquals(0L, response.getSessions().get(0).getServer().getHost().getSwap().getTotalBytes());
        assertEquals(testString, response.getSessions().get(0).getServer().getHost().getAws().getRegion());
        assertEquals(cpuVendor, response.getSessions().get(0).getServer().getHost().getCpuInfo().getVendor());
        assertEquals(0F, response.getSessions().get(0).getServer().getHost().getCpuLoadAverage().getOneMinute());
        assertEquals(testString, response.getSessions().get(0).getServer().getHost().getGpus().get(0).getVendor());
        assertEquals(testString, response.getSessions().get(0).getServer().getHost().getLoggedInUsers().get(0).getUsername());
        assertEquals(Protocol.HTTP.toString(), response.getSessions().get(0).getServer().getEndpoints().get(0).getProtocol());
        assertEquals(Availability.AVAILABLE.toString(), response.getSessions().get(0).getServer().getAvailability());
        assertEquals(UnavailabilityReason.UNKNOWN.toString(), response.getSessions().get(0).getServer().getUnavailabilityReason());
        assertNull(response.getError());

        host.setOs(null);
        host.setMemory(null);
        host.setSwap(null);
        host.setAws(null);
        host.setCpuInfo(null);
        host.setCpuLoadAverage(null);
        host.setGpus(null);
        host.addGpusItem(null);
        host.setLoggedInUsers(null);
        host.addLoggedInUsersItem(null);
        fullServer.setTags(null);
        fullServer.addTagsItem(null);

        fullServer.getEndpoints().get(0).setProtocol(Protocol.QUIC.toString());
        fullServer.setAvailability(Availability.UNAVAILABLE.toString());
        fullServer.setUnavailabilityReason(UnavailabilityReason.EXISTING_LOGGED_IN_USER.toString());
        token.setOperator(FilterToken.OperatorEnum.NOT_EQUAL);
        response = testBrokerClient.describeSessions(handlerRequest.addTagsItem(new KeyValuePair()));
        assertEquals(1, response.getSessions().size());
        assertEquals(Protocol.QUIC.toString(), response.getSessions().get(0).getServer().getEndpoints().get(0).getProtocol());
        assertEquals(Availability.UNAVAILABLE.toString(), response.getSessions().get(0).getServer().getAvailability());
        assertEquals(UnavailabilityReason.EXISTING_LOGGED_IN_USER.toString(), response.getSessions().get(0).getServer().getUnavailabilityReason());
        assertNull(response.getError());

        host.setGpus(null);
        host.setLoggedInUsers(null);
        fullServer.getEndpoints().get(0).setProtocol(null);
        fullServer.setTags(null);
        fullServer.setAvailability(null);
        fullServer.setUnavailabilityReason(UnavailabilityReason.SERVER_FULL.toString());
        response = testBrokerClient.describeSessions(handlerRequest.addTagsItem(null));
        assertEquals(1, response.getSessions().size());
        assertEquals(UnavailabilityReason.SERVER_FULL.toString(), response.getSessions().get(0).getServer().getUnavailabilityReason());
        assertNull(response.getError());

        fullServer.setHost(null);
        fullServer.setEndpoints(null);
        fullServer.addEndpointsItem(null);
        fullServer.setUnavailabilityReason(UnavailabilityReason.SERVER_CLOSED.toString());
        response = testBrokerClient.describeSessions(handlerRequest.tags(null));
        assertEquals(1, response.getSessions().size());
        assertEquals(UnavailabilityReason.SERVER_CLOSED.toString(), response.getSessions().get(0).getServer().getUnavailabilityReason());
        assertNull(response.getError());

        fullServer.setEndpoints(null);
        fullServer.setUnavailabilityReason(UnavailabilityReason.UNHEALTHY_DCV_SERVER.toString());
        response = testBrokerClient.describeSessions(handlerRequest);
        assertEquals(1, response.getSessions().size());
        assertEquals(UnavailabilityReason.UNHEALTHY_DCV_SERVER.toString(), response.getSessions().get(0).getServer().getUnavailabilityReason());
        assertNull(response.getError());

        fullServer.setUnavailabilityReason(UnavailabilityReason.UNREACHABLE_AGENT.toString());
        response = testBrokerClient.describeSessions(handlerRequest);
        assertEquals(1, response.getSessions().size());
        assertEquals(UnavailabilityReason.UNREACHABLE_AGENT.toString(), response.getSessions().get(0).getServer().getUnavailabilityReason());
        assertNull(response.getError());

        fullServer.setUnavailabilityReason(null);
        response = testBrokerClient.describeSessions(handlerRequest);
        assertEquals(1, response.getSessions().size());
        assertNull(response.getError());

        session.setServer(null);
        response = testBrokerClient.describeSessions(handlerRequest);
        assertEquals(1, response.getSessions().size());
        assertNull(response.getError());

        brokerResponse.setSessions(null);
        brokerResponse.addSessionsItem(null);
        response = testBrokerClient.describeSessions(handlerRequest);
        assertEquals(1, response.getSessions().size());
        assertNull(response.getError());

        brokerResponse.setSessions(null);
        response = testBrokerClient.describeSessions(handlerRequest);
        assertNull(response.getSessions());
        assertNull(response.getError());

        doReturn(null).when(mockSessionsApi).describeSessions(any());
        response = testBrokerClient.describeSessions(handlerRequest);
        assertNull(response);
    }

    @Test
    public void testDescribeSessionsHostNotReturned() throws ApiException {
        Session session = new Session().name(testString);
        FilterToken token = new FilterToken().operator(FilterToken.OperatorEnum.EQUAL).value(testString);
        DescribeSessionsUIRequestData handlerRequest = new DescribeSessionsUIRequestData().addSessionIdsItem(token);
        DescribeSessionsResponse brokerResponse = new DescribeSessionsResponse();

        Server sessionServer = new Server().id(serverId);
        sessionServer.addEndpointsItem(new Endpoint().protocol(Protocol.HTTP.toString()));
        sessionServer.addTagsItem(new broker.model.KeyValuePair());
        sessionServer.setAvailability(Availability.AVAILABLE.toString());

        session.server(sessionServer);

        brokerResponse.addSessionsItem(session);

        doReturn(brokerResponse).when(mockSessionsApi).describeSessions(any());

        doAnswer(invocation -> {
            DescribeServersRequestData requestData = invocation.getArgument(0);
            assertEquals(1, requestData.getServerIds().size());

            return new DescribeServersResponse().servers(List.of(new Server().id(testString)));
        }).when(mockServersApi).describeServers(any());

        DescribeSessionsUIResponse response = testBrokerClient.describeSessions(handlerRequest);

        assertEquals(1, response.getSessions().size());
        assertNotNull(response.getSessions().get(0));
        assertNotNull(response.getSessions().get(0).getServer());
        assertEquals(serverId, response.getSessions().get(0).getServer().getId());
        assertNull(response.getSessions().get(0).getServer().getIp());
    }

    @Test
    public void testGetSessionScreenshotsSuccess() throws Exception {
        GetSessionScreenshotsUIRequestData handlerRequest = new GetSessionScreenshotsUIRequestData()
                .addSessionIdsItem(testString)
                .maxWidth(maxWidth)
                .maxHeight(maxHeight);
        GetSessionScreenshotsResponse brokerResponse = new GetSessionScreenshotsResponse().addSuccessfulListItem(new GetSessionScreenshotSuccessfulResponse().sessionScreenshot(new SessionScreenshot().addImagesItem(new SessionScreenshotImage().data(testString))));
        brokerResponse.addUnsuccessfulListItem(new GetSessionScreenshotUnsuccessfulResponse().getSessionScreenshotRequestData(new GetSessionScreenshotRequestData()));
        doReturn(brokerResponse).when(mockSessionsApi).getSessionScreenshots(any());
        GetSessionScreenshotsUIResponse response = testBrokerClient.getSessionScreenshots(handlerRequest);
        assertEquals(1, response.getSuccessfulList().size());
        assertEquals(1, response.getUnsuccessfulList().size());
        assertEquals(testString, response.getSuccessfulList().get(0).getSessionScreenshot().getImages().get(0).getData());
        assertNull(response.getError());

        brokerResponse.getSuccessfulList().get(0).getSessionScreenshot().setImages(null);
        brokerResponse.getSuccessfulList().get(0).getSessionScreenshot().addImagesItem(null);
        brokerResponse.getUnsuccessfulList().get(0).setGetSessionScreenshotRequestData(null);
        response = testBrokerClient.getSessionScreenshots(handlerRequest.sessionIds(null));
        assertEquals(1, response.getSuccessfulList().size());
        assertEquals(1, response.getUnsuccessfulList().size());
        assertNull(response.getError());

        brokerResponse.getSuccessfulList().get(0).getSessionScreenshot().setImages(null);
        brokerResponse.setUnsuccessfulList(null);
        brokerResponse.addUnsuccessfulListItem(null);
        response = testBrokerClient.getSessionScreenshots(handlerRequest);
        assertEquals(1, response.getSuccessfulList().size());
        assertEquals(1, response.getUnsuccessfulList().size());
        assertNull(response.getError());

        brokerResponse.getSuccessfulList().get(0).setSessionScreenshot(null);
        brokerResponse.setUnsuccessfulList(null);
        response = testBrokerClient.getSessionScreenshots(handlerRequest);
        assertEquals(1, response.getSuccessfulList().size());
        assertNull(response.getUnsuccessfulList());
        assertNull(response.getError());

        brokerResponse.setSuccessfulList(null);
        brokerResponse.addSuccessfulListItem(null);
        response = testBrokerClient.getSessionScreenshots(handlerRequest);
        assertEquals(1, response.getSuccessfulList().size());
        assertNull(response.getError());

        brokerResponse.setSuccessfulList(null);
        response = testBrokerClient.getSessionScreenshots(handlerRequest);
        assertNull(response.getSuccessfulList());
        assertNull(response.getError());

        doReturn(null).when(mockSessionsApi).getSessionScreenshots(any());
        response = testBrokerClient.getSessionScreenshots(handlerRequest.addSessionIdsItem(testString));
        assertNull(response);
    }

    @Test
    public void testGetSessionScreenshotsWhenResolutionParametersAreZero() throws Exception{
        GetSessionScreenshotsUIRequestData handlerRequest = new GetSessionScreenshotsUIRequestData()
                .addSessionIdsItem(testString)
                .maxWidth(0L)
                .maxHeight(0L);

        GetSessionScreenshotsResponse brokerResponse = new GetSessionScreenshotsResponse().addSuccessfulListItem(new GetSessionScreenshotSuccessfulResponse().sessionScreenshot(new SessionScreenshot().addImagesItem(new SessionScreenshotImage().data(testString))));

        ArgumentCaptor<List<GetSessionScreenshotRequestData>> requestCaptor = ArgumentCaptor.forClass(List.class);
        when(mockSessionsApi.getSessionScreenshots(requestCaptor.capture()))
                .thenReturn(brokerResponse);

        GetSessionScreenshotsUIResponse response = testBrokerClient.getSessionScreenshots(handlerRequest);

        List<GetSessionScreenshotRequestData> capturedRequest = requestCaptor.getValue();
        assertFalse(capturedRequest.isEmpty());
        GetSessionScreenshotRequestData requestData = capturedRequest.get(0);
        assertEquals(testString, requestData.getSessionId());
        assertNull(requestData.getMaxWidth());
        assertNull(requestData.getMaxHeight());

        assertEquals(1, response.getSuccessfulList().size());
        assertEquals(testString, response.getSuccessfulList().get(0).getSessionScreenshot().getImages().get(0).getData());
        assertNull(response.getUnsuccessfulList());
        assertNull(response.getError());
    }

    @Test
    public void testValidateSessionTemplate() {
        CreateSessionTemplateRequestData request = new CreateSessionTemplateRequestData();
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request, false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.name(testString), false));

        // Test for a large name
        byte[] array = new byte[1000];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        assertThrowsExactly(BadRequestException.class,
                () -> testBrokerClient.validateSessionTemplate(request.name(generatedString), false));
        request.name(testString);

        // Test for a large description
        array = new byte[3000];
        new Random().nextBytes(array);
        String generatedDescription = new String(array, StandardCharsets.UTF_8);
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.description(generatedDescription), false));
        request.description(null);

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.osFamily(OsFamily.WINDOWS), false));

        when(mockSessionTemplateRepository.existsById(UUID.nameUUIDFromBytes(request.getName().getBytes()).toString())).thenReturn(true);
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request, false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.type(Type.VIRTUAL), false));

        request.setType(Type.CONSOLE);
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.initFile(testString), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.dcvGlEnabled(true), false));

        request.setDcvGlEnabled(null);
        request.setAutorunFile(testString);
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.osFamily(OsFamily.LINUX), false));

        request.type(Type.VIRTUAL);
        request.setAutorunFile(null);
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.addAutorunFileArgumentsItem(testString), false));

        request.setAutorunFile(testString);
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements(testString), false));

        request.setDcvGlEnabled(true);
        request.setAutorunFileArguments(null);
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements(testString), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("tag:test := 1"), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("(tag:test = 1"), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("tag:test = 1)"), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("tag:test > 'test')"), false));
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("tag:test >= 'test')"), false));
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("tag:test <= 'test')"), false));
        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("tag:test < 'test')"), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("server:test = 'test'"), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("server:Host.Os.Family = test"), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("server:"), false));

        assertThrowsExactly(BadRequestException.class, () -> testBrokerClient.validateSessionTemplate(request.requirements("tag:"), false));

        testBrokerClient.validateSessionTemplate(request.requirements("tag:color = 'pink' and (server:Host.Os.Family = 'windows' or tag:color := 'red')"), true);

        testBrokerClient.validateSessionTemplate(request.requirements(null), true);
    }

    @Test
    public void createSessionsSuccess() throws ApiException {
        Session session = new Session().id(testString);
        List<String> autorunFileArguments = List.of(testString);
        CreateSessionRequestData brokerRequest = new CreateSessionRequestData().autorunFileArguments(autorunFileArguments);
        CreateSessionsResponse brokerResponse = new CreateSessionsResponse().addSuccessfulListItem(session);
        brokerResponse.setRequestId(testString);
        brokerResponse.addUnsuccessfulListItem(new UnsuccessfulCreateSessionRequestData().createSessionRequestData(brokerRequest));
        brokerResponse.addUnsuccessfulListItem(new UnsuccessfulCreateSessionRequestData().createSessionRequestData(null));
        CreateSessionUIRequestData request = new CreateSessionUIRequestData().name(testString);
        SessionTemplate sessionTemplate = new SessionTemplate().id(idString).requirements(testString);
        doReturn(brokerResponse).when(mockSessionsApi).createSessions(any());
        CreateSessionsUIResponse response = testBrokerClient.createSessions(Arrays.asList(new Pair<>(request.owner(testString), sessionTemplate), new Pair<>(request, sessionTemplate), new Pair<>(request, sessionTemplate)));
        assertNull(response.getError());
        assertEquals(2, response.getUnsuccessfulList().size());
        assertEquals(autorunFileArguments, response.getUnsuccessfulList().get(0).getCreateSessionRequestData().getAutorunFileArguments());
        assertEquals(1, response.getUnsuccessfulList().get(0).getFailureReasons().size());
        assertTrue(response.getUnsuccessfulList().get(0).getFailureReasons().containsKey("Broker"));
        assertNull(response.getUnsuccessfulList().get(1).getCreateSessionRequestData());
        assertEquals(1, response.getUnsuccessfulList().get(1).getFailureReasons().size());
        assertTrue(response.getUnsuccessfulList().get(1).getFailureReasons().containsKey("Broker"));
        assertEquals(1, response.getSuccessfulList().size());
        assertEquals(session.getId(), response.getSuccessfulList().get(0).getId());

        response = testBrokerClient.createSessions(List.of(new Pair<>(new CreateSessionUIRequestData(), null)));
        assertEquals(3, response.getUnsuccessfulList().get(0).getFailureReasons().size());
        assertTrue(response.getUnsuccessfulList().get(0).getFailureReasons().containsKey("Name"));
        assertTrue(response.getUnsuccessfulList().get(0).getFailureReasons().containsKey("Owner"));
        assertTrue(response.getUnsuccessfulList().get(0).getFailureReasons().containsKey("Session Template"));
        assertNull(response.getUnsuccessfulList().get(0).getCreateSessionRequestData().getName());
        assertNull(response.getUnsuccessfulList().get(0).getCreateSessionRequestData().getOwner());
    }

    @Test
    public void getSessionConnectionDataSuccess() throws ApiException {
        GetSessionConnectionDataResponse brokerResponse = new GetSessionConnectionDataResponse().session(new Session().id(testString));
        doReturn(brokerResponse.connectionToken(testString)).when(mockGetSessionConnectionDataApi).getSessionConnectionData(any(), any());
        GetSessionConnectionDataUIResponse response = testBrokerClient.getSessionConnectionData(testString, testString);
        assertNull(response.getError());
        assertEquals(testString, response.getSession().getId());
        assertEquals(testString, response.getConnectionToken());

        doReturn(null).when(mockGetSessionConnectionDataApi).getSessionConnectionData(any(), any());
        response = testBrokerClient.getSessionConnectionData(testString, testString);
        assertNull(response);
    }

    @Test
    public void deleteSessionSuccess() throws ApiException {
        List<DeleteSessionUIRequestData> deleteSessionUIRequestData = new ArrayList<>();
        deleteSessionUIRequestData.add(new DeleteSessionUIRequestData().sessionId(idString).owner(userString));
        deleteSessionUIRequestData.add(new DeleteSessionUIRequestData().sessionId(testString).owner(userString));

        DeleteSessionsResponse brokerResponse = new DeleteSessionsResponse().addSuccessfulListItem(new DeleteSessionSuccessfulResponse().sessionId(idString).state(TEST_STATE.toString()));
        brokerResponse.addUnsuccessfulListItem(new DeleteSessionUnsuccessfulResponse().sessionId(testString).failureReason(testString));
        when(mockSessionsApi.deleteSessions(any())).thenReturn(brokerResponse);
        DeleteSessionsUIResponse response = testBrokerClient.deleteSessions(deleteSessionUIRequestData);
        assertEquals(1, response.getSuccessfulList().size());
        assertEquals(idString, response.getSuccessfulList().get(0).getSessionId());
        assertEquals(TEST_STATE, response.getSuccessfulList().get(0).getState());
        assertEquals(1, response.getUnsuccessfulList().size());
        assertEquals(testString, response.getUnsuccessfulList().get(0).getSessionId());
        assertTrue(response.getUnsuccessfulList().get(0).getFailureReasons().containsKey("Broker"));
        assertNull(response.getError());

        deleteSessionUIRequestData.get(0).owner(null).sessionId(null);
        deleteSessionUIRequestData.remove(1);
        response = testBrokerClient.deleteSessions(deleteSessionUIRequestData);
        assertEquals(1, response.getUnsuccessfulList().size());
        assertTrue(response.getUnsuccessfulList().get(0).getFailureReasons().containsKey("Owner"));
        assertTrue(response.getUnsuccessfulList().get(0).getFailureReasons().containsKey("SessionId"));
        assertNull(response.getSuccessfulList());
        assertNull(response.getError());
    }

    @Test
    public void testDeleteSessionsBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> {
                    doThrow(new ApiException(400, "Error code 400")).when(mockSessionsApi).deleteSessions(any());
                    testBrokerClient.deleteSessions(List.of(new DeleteSessionUIRequestData().sessionId(idString).owner(userString)));
                });
    }

    @Test
    public void testDeleteSessionsBrokerClientException() {
        assertThrowsExactly(BrokerClientException.class,
                () -> {
                    doThrow(ApiException.class).when(mockSessionsApi).deleteSessions(any());
                    testBrokerClient.deleteSessions(List.of(new DeleteSessionUIRequestData().sessionId(idString).owner(userString)));
                });
    }
}
