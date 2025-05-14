package handler.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServerClientServiceTest {
    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthServerClientService authServerClientService;

    @Mock
    private HttpResponse<String> httpResponse;

    private static final String TEST_ENDPOINT = "https://test-endpoint.com";
    private static final String TEST_USERINFO_ENDPOINT = "https://test-userinfo-endpoint.com";
    private static final String TEST_ACCESS_TOKEN = "test-access-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authServerClientService, "userinfoEndpoint", TEST_ENDPOINT);
    }

    @Test
    void getUserInfo_WithUserInfoEndpoint_Success() throws Exception {
        when(httpResponse.body()).thenReturn("{}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(new HashMap<>());

        Map<String, Object> result = authServerClientService.getUserInfo(TEST_ACCESS_TOKEN);

        assertNotNull(result);
        verify(httpClient).send(any(), any());
        verify(httpResponse).body();
        verify(objectMapper).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void getUserInfo_WithWellKnownEndpoint_Success() throws Exception {
        ReflectionTestUtils.setField(authServerClientService, "userinfoEndpoint", null);
        ReflectionTestUtils.setField(authServerClientService, "wellknownEndpoint", TEST_ENDPOINT);

        Map<String, Object> wellKnownResponse = new HashMap<>();
        wellKnownResponse.put("userinfo_endpoint", TEST_USERINFO_ENDPOINT);

        Map<String, Object> userInfoResponse = new HashMap<>();

        when(httpResponse.body()).thenReturn("{}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(wellKnownResponse)
                .thenReturn(userInfoResponse);

        Map<String, Object> result = authServerClientService.getUserInfo(TEST_ACCESS_TOKEN);

        assertNotNull(result);
        verify(httpClient, times(2)).send(any(), any());
        verify(httpResponse, times(2)).body();
        verify(objectMapper, times(2)).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void getUserInfo_HttpClientThrowsException_ReturnsNull() throws Exception {
        when(httpClient.send(any(), any())).thenThrow(new RuntimeException("Network error"));

        Map<String, Object> result = authServerClientService.getUserInfo(TEST_ACCESS_TOKEN);

        assertNull(result);
        verify(httpClient).send(any(), any());
        verify(objectMapper, times(0)).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void getUserInfo_ObjectMapperThrowsException_ReturnsNull() throws Exception {
        when(httpResponse.body()).thenReturn("{}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenThrow(new RuntimeException("Parsing error"));

        Map<String, Object> result = authServerClientService.getUserInfo(TEST_ACCESS_TOKEN);

        assertNull(result);
        verify(httpClient).send(any(), any());
        verify(httpResponse).body();
        verify(objectMapper).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void getUserInfo_WellKnownEndpointReturnsNull_ReturnsNull() throws Exception {
        ReflectionTestUtils.setField(authServerClientService, "userinfoEndpoint", null);
        ReflectionTestUtils.setField(authServerClientService, "wellknownEndpoint", TEST_ENDPOINT);

        when(httpResponse.body()).thenReturn("{}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(null);

        Map<String, Object> result = authServerClientService.getUserInfo(TEST_ACCESS_TOKEN);

        assertNull(result);
        verify(httpClient).send(any(), any());
        verify(httpResponse).body();
        verify(objectMapper).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void getUserInfo_WellKnownResponseMissingEndpoint_ReturnsNull() throws Exception {
        ReflectionTestUtils.setField(authServerClientService, "userinfoEndpoint", null);
        ReflectionTestUtils.setField(authServerClientService, "wellknownEndpoint", TEST_ENDPOINT);

        Map<String, Object> wellKnownResponse = new HashMap<>(); // No userinfo_endpoint

        when(httpResponse.body()).thenReturn("{}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(wellKnownResponse);

        Map<String, Object> result = authServerClientService.getUserInfo(TEST_ACCESS_TOKEN);

        assertNull(result);
        verify(httpClient).send(any(), any());
        verify(httpResponse).body();
        verify(objectMapper).readValue(anyString(), any(TypeReference.class));
    }
}