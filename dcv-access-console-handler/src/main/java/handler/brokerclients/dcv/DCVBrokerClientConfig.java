package handler.brokerclients.dcv;

import broker.api.GetSessionConnectionDataApi;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import broker.api.SessionsApi;
import broker.api.ServersApi;
import broker.ApiClient;

@Configuration
public class DCVBrokerClientConfig {
    @Bean
    @Autowired
    @Scope("prototype")
    public SessionsApi provideSessionsApi(@Value("${client-to-broker-connector-url}") String baseApiUrl, @Value("${client-to-broker-connection-verify-ssl}") boolean verifySsl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseApiUrl);
        apiClient.setVerifyingSsl(verifySsl);
        return new SessionsApi(apiClient);
    }

    @Bean
    @Autowired
    @Scope("prototype")
    public ServersApi provideServersApi(@Value("${client-to-broker-connector-url}") String baseApiUrl, @Value("${client-to-broker-connection-verify-ssl}") boolean verifySsl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseApiUrl);
        apiClient.setVerifyingSsl(verifySsl);
        return new ServersApi(apiClient);
    }

    @Bean
    @Autowired
    @Scope("prototype")
    public GetSessionConnectionDataApi provideGetSessionConnectionDataApi(@Value("${client-to-broker-connector-url}") String baseApiUrl, @Value("${client-to-broker-connection-verify-ssl}") boolean verifySsl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseApiUrl);
        apiClient.setVerifyingSsl(verifySsl);
        return new GetSessionConnectionDataApi(apiClient);
    }
}
