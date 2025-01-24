package integration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Slf4j
public class AuthServerUtils {

    private static final int CODE_VERIFIER_LENGTH = 36;

    private static final String STATE = "authorizationHydraTestState";

    private static final String NONCE = "authorizationHydraTestNonce";
    private static final String ACCESS_TOKEN_PARAMETER_NAME = "access_token";
    private static final String TOKEN_TYPE_PARAMETER_NAME = "token_type";
    private static final String BEARER_TOKEN_FORMAT_STRING = "%s %s";
    private static final String SHA256_STRING = "SHA-256";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String CODE_CHALLENGE_PARAMETER_NAME = "code_challenge";
    private static final String LOCATION_HEADER_NAME = "Location";
    private static final String USERNAME_HEADER_NAME = "username";
    private static final String CODE_PARAMETER_NAME = "code";
    private static final String CODE_VERIFIER_PARAMETER_NAME = "code_verifier";
    private static final String STATE_PARAMETER_NAME = "state";

    private static final Header URL_ENCODED_CONTENT_TYPE_HEADER = new BasicHeader("Content-type", "application/x-www-form-urlencoded");
    private static final Header JSON_CONTENT_TYPE_HEADER = new BasicHeader("Content-type", "application/json");
    public static final Header ACCEPT_JSON_HEADER = new BasicHeader("Accept", "application/json");

    private static final String CLIENT_ID_PARAMETER_PREFIX = "/integTestingSecrets/authId/";
    private static final String CLIENT_SECRET_PARAMETER_PREFIX = "/integTestingSecrets/authSecret/";
    private static final String SSM_REGION = "us-west-2";

    private final ObjectMapper objectMapper;

    private final String authServerRedirectUri;

    private final List<NameValuePair> authNVPs;

    private final HashMap<String, Header> userHeaderMap;
    private final CloseableHttpClient httpClient;

    private final String loginEndpoint;
    private final String authorizeEndpoint;
    private final String tokenEndpoint;

    public AuthServerUtils(String instanceDns, String ssmParameterSuffix) {
        loginEndpoint = instanceDns + "/login";
        authorizeEndpoint = instanceDns + "/oauth2/authorize";
        tokenEndpoint = instanceDns + "/oauth2/token";
        authServerRedirectUri = instanceDns + "/api/auth/callback/dcv-access-console-auth-server";

        String authIdParameterName = CLIENT_ID_PARAMETER_PREFIX + ssmParameterSuffix;
        String authSecretParameterName = CLIENT_SECRET_PARAMETER_PREFIX + ssmParameterSuffix;

        String clientId = retrieveParameter(authIdParameterName);
        String clientSecret = retrieveParameter(authSecretParameterName);

        log.info("Retrieved clientID {} and clientSecret {}", clientId, clientSecret);

        objectMapper = new ObjectMapper();
        userHeaderMap = new HashMap<>();

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            CustomTrustManager trustManager = new CustomTrustManager();
            sslContext.init(null, new CustomTrustManager[]{trustManager}, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Unable to setup custom Trust Manager.");
            throw new RuntimeException(e);
        }

        httpClient = HttpClients.custom().disableRedirectHandling().setSSLContext(sslContext).build();


        authNVPs = new ArrayList<>(List.of(
                new BasicNameValuePair("redirect_uri", authServerRedirectUri),
                new BasicNameValuePair("response_type", "code"),
                new BasicNameValuePair("scope", "openid"),
                new BasicNameValuePair("state", STATE),
                new BasicNameValuePair("code_challenge_method", "S256"),
                new BasicNameValuePair("authentication_method", "client_secret_basic"),
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("nonce", NONCE)
        ));
        authNVPs.add(new BasicNameValuePair("client_id", clientId));
        authNVPs.add(new BasicNameValuePair("client_secret", clientSecret));
    }

    private String retrieveParameter(String parameterName) {
        Region region = Region.of(SSM_REGION);
        try (SsmClient client = SsmClient.builder()
                .region(region)
                .build()) {
            GetParameterRequest getParameterRequest = GetParameterRequest.builder()
                    .name(parameterName)
                    .build();
            GetParameterResponse getParameterResponse = client.getParameter(getParameterRequest);

            return getParameterResponse.parameter().value();
        }
    }

    private void loginToAuthServer(CloseableHttpClient httpClient, String username) throws IOException {
        HttpGet loginHttpGet = new HttpGet(this.loginEndpoint);
        Header usernameHeader = new BasicHeader(USERNAME_HEADER_NAME, username);
        loginHttpGet.addHeader(usernameHeader);
        try (CloseableHttpResponse response = httpClient.execute(loginHttpGet)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                    String.format("Request failed with status %s and headers %s", response.getStatusLine(), Arrays.toString(response.getAllHeaders())));
            log.info("Successfully logged into auth server with user {}", username);
        }
    }

    private String getCodeFromAuthServer(CloseableHttpClient httpClient, String username, String codeChallenge) throws URISyntaxException, IOException {
        HttpGet authHttpGet = new HttpGet(this.authorizeEndpoint);
        Header usernameHeader = new BasicHeader(USERNAME_HEADER_NAME, username);
        authHttpGet.addHeader(usernameHeader);

        URI authURI = new URIBuilder(authHttpGet.getURI())
                .addParameters(authNVPs)
                .addParameter(CODE_CHALLENGE_PARAMETER_NAME, codeChallenge)
                .build();

        authHttpGet.setURI(authURI);

        log.info("Attempting to get code from AuthServer at {} for user {}", this.authorizeEndpoint, username);
        try (CloseableHttpResponse response = httpClient.execute(authHttpGet)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                    String.format("Request failed with status %s and headers %s", response.getStatusLine(), Arrays.toString(response.getAllHeaders())));
            assertNotNull(response.getHeaders(LOCATION_HEADER_NAME));
            assertEquals(response.getHeaders(LOCATION_HEADER_NAME).length, 1);
            assertNotNull(response.getHeaders(LOCATION_HEADER_NAME)[0]);

            URI location = new URIBuilder(response.getHeaders(LOCATION_HEADER_NAME)[0].getValue()).build();

            List<NameValuePair> params = URLEncodedUtils.parse(location, StandardCharsets.UTF_8);

            assertNotNull(params);
            assertEquals(params.size(), 2);
            assertEquals(params.get(0).getName(), CODE_PARAMETER_NAME);
            assertEquals(params.get(1).getName(), STATE_PARAMETER_NAME);
            assertEquals(params.get(1).getValue(), STATE);
            assertNotNull(params.get(0).getValue());

            log.info("Successfully retrieved code for user {}", username);
            return params.get(0).getValue();
        }
    }

    private String getAccessTokenFromAuthServer(CloseableHttpClient httpClient, String codeVerifier, String code) throws IOException, URISyntaxException {
        HttpPost tokenHttpPost = new HttpPost(this.tokenEndpoint);
        tokenHttpPost.addHeader(ACCEPT_JSON_HEADER);
        tokenHttpPost.addHeader(URL_ENCODED_CONTENT_TYPE_HEADER);

        URI tokenURI = new URIBuilder(tokenHttpPost.getURI())
                .addParameters(authNVPs)
                .addParameter(CODE_PARAMETER_NAME, code)
                .addParameter(CODE_VERIFIER_PARAMETER_NAME, codeVerifier)
                .build();

        tokenHttpPost.setURI(tokenURI);

        log.info("Attempting to retrieve access token from {} using code {}", this.tokenEndpoint, code);
        try (CloseableHttpResponse response = httpClient.execute(tokenHttpPost)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    String.format("Request failed with status %s and headers %s", response.getStatusLine(), Arrays.toString(response.getAllHeaders())));
            TypeReference<HashMap<String,String>> typeRef = new TypeReference<>() {};
            HashMap<String, String> tokenResponse = objectMapper.readValue(EntityUtils.toString(response.getEntity()), typeRef);

            assertNotNull(tokenResponse);
            assertTrue(tokenResponse.containsKey(ACCESS_TOKEN_PARAMETER_NAME));
            assertTrue(tokenResponse.containsKey(TOKEN_TYPE_PARAMETER_NAME));

            String tokenName = tokenResponse.get(ACCESS_TOKEN_PARAMETER_NAME);
            log.info("Successfully retrieved token with name {}", tokenName);
            return String.format(BEARER_TOKEN_FORMAT_STRING, tokenResponse.get(TOKEN_TYPE_PARAMETER_NAME), tokenName);
        }
    }

    public Header getAuthorizationHeader(String username) {
        if (userHeaderMap.containsKey(username)) {
            return userHeaderMap.get(username);
        } else {
            Header header = getAuthorizationHeaderFromAuthServer(username);
            userHeaderMap.put(username, header);
            return header;
        }
    }

    private Header getAuthorizationHeaderFromAuthServer(String username) {
        try {
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallengeHash(codeVerifier);
            loginToAuthServer(httpClient, username);
            String code = getCodeFromAuthServer(httpClient, username, codeChallenge);
            String accessToken = getAccessTokenFromAuthServer(httpClient, codeVerifier, code);
            return new BasicHeader(AUTHORIZATION_HEADER_NAME, accessToken);
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect.", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI Syntax Failure", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm Missing", e);
        }
    }

    public HashMap<String,Object> makeHandlerMultipartPostCall(Header authorizationHeader, CloseableHttpClient httpClient, String endpoint, HttpEntity entity, boolean expectAuthorized) throws IOException {
        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setEntity(entity);
        return makeHandlerCall(httpPost, authorizationHeader, httpClient, expectAuthorized);
    }

    public HashMap<String,Object> makeHandlerPostCall(Header authorizationHeader, CloseableHttpClient httpClient, String endpoint, String body, boolean expectAuthorized) throws IOException {
        HttpPost httpPost = new HttpPost(endpoint);
        StringEntity entity = new StringEntity(body);
        httpPost.setEntity(entity);
        httpPost.setHeader(JSON_CONTENT_TYPE_HEADER);
        return makeHandlerCall(httpPost, authorizationHeader, httpClient, expectAuthorized);
    }

    public HashMap<String,Object> makeHandlerPutCall(Header authorizationHeader, CloseableHttpClient httpClient, String endpoint, String body, boolean expectAuthorized) throws IOException {
        HttpPut httpPut = new HttpPut(endpoint);
        StringEntity entity = new StringEntity(body);
        httpPut.setEntity(entity);
        httpPut.setHeader(JSON_CONTENT_TYPE_HEADER);
        return makeHandlerCall(httpPut, authorizationHeader, httpClient, expectAuthorized);
    }
    public HashMap<String,Object> makeHandlerDeleteCall(Header authorizationHeader, CloseableHttpClient httpClient, String endpoint, String body, boolean expectAuthorized) throws IOException {
        HttpEntityEnclosingRequestBase httpDelete = new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return "DELETE";
            }
        };
        httpDelete.setURI(URI.create(endpoint));
        StringEntity entity = new StringEntity(body);
        httpDelete.setEntity(entity);
        httpDelete.setHeader(JSON_CONTENT_TYPE_HEADER);
        return makeHandlerCall(httpDelete, authorizationHeader, httpClient, expectAuthorized);
    }

    public HashMap<String, Object> makeHandlerPostCallNoEntity(Header authorizationHeader, CloseableHttpClient httpClient, String endpoint, boolean expectAuthorized) throws IOException {
        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader(ACCEPT_JSON_HEADER);
        return makeHandlerCall(httpPost, authorizationHeader, httpClient, expectAuthorized);
    }


    private HashMap<String, Object> makeHandlerCall(HttpRequestBase request, Header authorizationHeader, CloseableHttpClient httpClient, boolean expectAuthorized) {
        request.setHeader(ACCEPT_JSON_HEADER);
        request.setHeader(authorizationHeader);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (expectAuthorized) {
                assertEquals(statusCode, HttpStatus.SC_OK);
                TypeReference<HashMap<String,Object>> typeRef = new TypeReference<>() {};
                return objectMapper.readValue(EntityUtils.toString(response.getEntity()), typeRef);
            } else {
                List<Integer> expectedCodes = List.of(HttpStatus.SC_FORBIDDEN, HttpStatus.SC_UNAUTHORIZED);
                assertTrue(expectedCodes.contains(statusCode));
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect.", e);
        }
    }

    private static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[CODE_VERIFIER_LENGTH];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeChallengeHash(String codeVerifierValue) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(SHA256_STRING);
        byte[] codeVerifierBytes = codeVerifierValue.getBytes(StandardCharsets.US_ASCII);
        byte[] digest = messageDigest.digest(codeVerifierBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
