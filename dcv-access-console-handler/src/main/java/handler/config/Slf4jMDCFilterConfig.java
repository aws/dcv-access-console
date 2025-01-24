package handler.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class Slf4jMDCFilterConfig {
    public static final String DEFAULT_RESPONSE_TOKEN_HEADER = "Response_Token";
    public static final String DEFAULT_MDC_UUID_TOKEN_KEY = "Slf4jMDCFilter.UUID";
    public static final String DEFAULT_MDC_CLIENT_IP_KEY = "Slf4jMDCFilter.ClientIP";

    public static final String DEFAULT_MDC_PRINCIPAL_KEY = "Slf4jMDCFilter.Principal";

}
