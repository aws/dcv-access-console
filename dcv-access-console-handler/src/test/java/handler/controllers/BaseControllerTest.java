package handler.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import handler.authorization.SessionManagerAuthorizationManager;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.config.AuthenticationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import handler.throttling.AbstractConsumptionProbe;
import handler.throttling.AbstractThrottler;
import handler.throttling.AbstractThrottlingService;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;


@WithMockUser("test-user")
@Import(AuthenticationConfig.class)
public class BaseControllerTest {

    @MockBean
    protected AbstractThrottlingService mockThrottlingService;

    @MockBean
    protected AbstractThrottler mockThrottler;

    @MockBean
    protected AbstractConsumptionProbe mockProbe;

    @MockBean
    protected AbstractAuthorizationEngine mockAuthorizationEngine;

    @MockBean
    private SessionManagerAuthorizationManager mockSessionManagerAuthorizationManager;

    @MockBean
    private JwtDecoder jwtDecoder;

    protected static final String testUser = "test-user"; // This is the value that the SecurityContextHolder has for the username during Unit Tests.

    @BeforeEach
    public void setup() {
        throttlingSetup();
    }

    public void throttlingSetup() {
        when(mockProbe.isConsumed()).thenReturn(true);
        when(mockThrottler.tryConsumeAndReturnRemaining(1)).thenReturn(mockProbe);
        when(mockThrottlingService.getThrottler(any())).thenReturn(mockThrottler);
    }
}
