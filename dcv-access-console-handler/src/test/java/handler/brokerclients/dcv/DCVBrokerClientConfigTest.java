// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.brokerclients.dcv;

import broker.api.GetSessionConnectionDataApi;
import broker.api.SessionsApi;
import broker.api.ServersApi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DCVBrokerClientConfigTest {
    @InjectMocks
    private DCVBrokerClientConfig testConfig;
    @Value("${client-to-broker-connector-url}")
    private String baseApiUrl;
    @Value("${client-to-broker-connection-verify-ssl}")
    private boolean verifySsl;

    @Test
    public void testProvideSessionsApi() {
        SessionsApi sessionsApi = testConfig.provideSessionsApi(null, false);
        assertEquals(baseApiUrl, sessionsApi.getApiClient().getBasePath());
        assertEquals(verifySsl, sessionsApi.getApiClient().isVerifyingSsl());
    }

    @Test
    public void testProvideServersApi() {
        ServersApi serversApi = testConfig.provideServersApi(null, false);
        assertEquals(baseApiUrl, serversApi.getApiClient().getBasePath());
        assertEquals(verifySsl, serversApi.getApiClient().isVerifyingSsl());
    }

    @Test
    public void testProvideGetSessionConnectionDataApi() {
        GetSessionConnectionDataApi getSessionConnectionDataApi = testConfig.provideGetSessionConnectionDataApi(null, false);
        assertEquals(baseApiUrl, getSessionConnectionDataApi.getApiClient().getBasePath());
        assertEquals(verifySsl, getSessionConnectionDataApi.getApiClient().isVerifyingSsl());
    }
}
