// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.brokerclients.dcv;

import handler.exceptions.BrokerAuthenticationException;

import java.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DCVBrokerTokenClient {
    private static final String CREDENTIALS_TYPE = "?grant_type=client_credentials";
    private static final String CONTENT_TYPE_HEADER = "application/x-www-form-urlencoded";
    private final String AUTHORIZATION_HEADER;

    private final int NUMBER_OF_RETRIES = 3;

    private final String tokenUri;
    private final String clientCredentials;
    private final String authUrl;
    private final String clientId;
    private final String clientPassword;
    private TokenResult tokenResult;
    private long lastTokenTimestamp = 0;
    private final double tokenExpirationMargin = 0.8;

    Gson gson;

    @Autowired
    public DCVBrokerTokenClient(@Value("${client-to-broker-connector-auth-url}") String authUrl,
                                     @Value("${broker-client-id}") String clientId, @Value("${broker-client-password}") String clientPassword) {
        this.authUrl = authUrl;
        this.clientId = clientId;
        this.clientPassword = clientPassword;

        tokenUri = authUrl + CREDENTIALS_TYPE;
        String baseAuthCreds = String.format("%s:%s", clientId, clientPassword);
        clientCredentials = Base64.getEncoder().encodeToString(baseAuthCreds.getBytes());
        AUTHORIZATION_HEADER = "Basic " + clientCredentials;

        gson = new Gson();
    }

    private boolean hasTokenExpired() {
        return System.currentTimeMillis() - lastTokenTimestamp > tokenResult.expires_in * 1000 * tokenExpirationMargin;
    }

    public final String getToken() throws BrokerAuthenticationException {
        if(tokenResult == null || hasTokenExpired()) {
            for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
                try {
                    renewToken();
                    break;
                } catch (Exception e) {
                    log.error("Error while renewing token", e);
                    if (i == NUMBER_OF_RETRIES - 1) {
                        log.warn("Unable to renew token after {} retries. Not trying again...", NUMBER_OF_RETRIES);
                        throw new BrokerAuthenticationException(e);
                    }
                }
            }
        }
        return tokenResult.access_token;
    }

    private void renewToken() throws BrokerAuthenticationException {
        log.info("Attempting to renew token from endpoint {}", tokenUri);
        try(CloseableHttpClient client = getHttpClient()) {
            HttpUriRequest request = RequestBuilder.post().setUri(tokenUri)
                    .setHeader(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
                    .setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_HEADER).build();
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            if (response.getStatusLine().getStatusCode() == 200) {
                tokenResult = parseTokenResult(responseString);
                log.info("Successfully retrieved new token. Token expiration: {} seconds", tokenResult.expires_in);
                lastTokenTimestamp = System.currentTimeMillis();
            }
            else {
                throw new BrokerAuthenticationException(responseString);
            }
        }
        catch (Exception e) {
            throw new BrokerAuthenticationException(e);
        }
    }

    private TokenResult parseTokenResult(String jsonResult) throws JsonSyntaxException {
        return gson.fromJson(jsonResult, TokenResult.class);
    }

    private CloseableHttpClient getHttpClient() throws Exception {
        return HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
    }

    public static class TokenResult {
        public String access_token = null;
        public String token_type = null;
        public int expires_in = 0;
    }
}