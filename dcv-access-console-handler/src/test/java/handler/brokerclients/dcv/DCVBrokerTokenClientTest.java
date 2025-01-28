// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.brokerclients.dcv;

import handler.exceptions.BrokerAuthenticationException;

import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicStatusLine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
public class DCVBrokerTokenClientTest {
    @InjectMocks
    private DCVBrokerTokenClient testTokenClient;
    @Mock
    private CloseableHttpResponse mockHttpResponse;
    @Mock
    private HttpEntity mockHttpEntity;
    private final static String testResponse = "{\"access_token\": \"test-token\", \"token_type\": \"bearer\", \"expires_in\": 3600}";
    private final static String testExpiredResponse = "{\"access_token\": \"expired-token\", \"token_type\": \"bearer\", \"expires_in\": 0}";
    private final static String testToken = "test-token";

    @BeforeAll
    public static void setUpHttpClients() {
        mockStatic(HttpClients.class);
    }

    private void mockRenewToken() throws Exception {
        HttpClientBuilder mockHttpClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        when(HttpClients.custom()).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.setSSLContext(any())).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.setSSLHostnameVerifier(any())).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);
        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
    }

    @Test
    public void testTokenNotExpired() throws Exception {
        mockRenewToken();
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream(testResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(mock(ProtocolVersion.class), 200, null));
        testTokenClient.getToken();
        assertEquals(testToken, testTokenClient.getToken());
        verify(mockHttpEntity, times(1)).getContent();
    }

    @Test
    public void testTokenExpired() throws Exception {
        mockRenewToken();
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream(testExpiredResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(mock(ProtocolVersion.class), 200, null));
        testTokenClient.getToken();

        Thread.sleep(1);
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream(testResponse.getBytes(StandardCharsets.UTF_8)));
        assertEquals(testToken, testTokenClient.getToken());
        verify(mockHttpEntity, times(2)).getContent();
    }

    @Test
    public void testBadStatusCode() {
        assertThrowsExactly(BrokerAuthenticationException.class,
                () -> {
                    mockRenewToken();
                    when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream(testResponse.getBytes(StandardCharsets.UTF_8)));
                    when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(mock(ProtocolVersion.class), 0, null));
                    testTokenClient.getToken();
                });
    }

    @Test
    public void testRenewTokenFail() {
        assertThrowsExactly(BrokerAuthenticationException.class,
                () -> testTokenClient.getToken());
    }

    @Test
    public void testRenewTokenSuccess() throws Exception {
        mockRenewToken();
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream(testResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(mock(ProtocolVersion.class), 200, null));
        assertEquals(testToken, testTokenClient.getToken());
    }


    @Test
    public void testRenewTokenWithRetryFails() throws Exception {
        mockRenewToken();
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream(testResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockHttpResponse.getStatusLine()).thenReturn(
                new BasicStatusLine(mock(ProtocolVersion.class), 400, null),
                new BasicStatusLine(mock(ProtocolVersion.class), 400, null),
                new BasicStatusLine(mock(ProtocolVersion.class), 400, null),
                new BasicStatusLine(mock(ProtocolVersion.class), 400, null)
        );
        assertThrowsExactly(BrokerAuthenticationException.class,
                () -> testTokenClient.getToken());
    }


    @Test
    public void testRenewTokenWithRetrySucceeds() throws Exception {
        mockRenewToken();
        when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream(testResponse.getBytes(StandardCharsets.UTF_8)), new ByteArrayInputStream(testResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockHttpResponse.getStatusLine()).thenReturn(
                new BasicStatusLine(mock(ProtocolVersion.class), 400, null),
                new BasicStatusLine(mock(ProtocolVersion.class), 200, null)
        );
        assertEquals(testToken, testTokenClient.getToken());
    }
}
