package handler.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnExpression("!'${auth-server-userinfo-endpoint:}'.isEmpty() || !'${auth-server-well-known-uri:}'.isEmpty()")
public class AuthServerClientService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${auth-server-userinfo-endpoint:#{null}}")
    private String userinfoEndpoint;

    @Value("${auth-server-well-known-uri:#{null}}")
    private String wellknownEndpoint;

    public Map<String, Object> getUserInfo(String accessToken) {
        String endpoint = userinfoEndpoint;
        if (StringUtils.isEmpty(endpoint)) {
            Map<String, Object> wellknownResponse = getHttpResponse(wellknownEndpoint, null, null);
            if (wellknownResponse != null && wellknownResponse.containsKey("userinfo_endpoint")) {
                endpoint = wellknownResponse.get("userinfo_endpoint").toString();
            }
        }

        return getHttpResponse(endpoint, "Authorization", "Bearer " + accessToken);
    }

    private Map<String, Object> getHttpResponse(String endpoint, String headerName, String headerValue) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .GET();
            if (!StringUtils.isEmpty(headerName) && !StringUtils.isEmpty(headerValue)) {
                requestBuilder.header(headerName, headerValue);
            }
            HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            log.error("Error retrieving/parsing response from {}", endpoint, e);
            return null;
        }
    }
}