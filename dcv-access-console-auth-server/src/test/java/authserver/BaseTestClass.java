package authserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import authserver.throttling.AbstractConsumptionProbe;
import authserver.throttling.AbstractThrottler;
import authserver.throttling.AbstractThrottlingService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

public class BaseTestClass {

    @MockBean
    protected AbstractThrottlingService mockThrottlingService;

    @MockBean
    protected AbstractThrottler mockThrottler;

    @MockBean
    protected AbstractConsumptionProbe mockProbe;

    @BeforeEach
    public void setup() {
        throttlingSetup();
    }

    public void throttlingSetup() {
        when(mockProbe.isConsumed()).thenReturn(true);
        when(mockThrottler.tryConsumeAndReturnRemaining(1)).thenReturn(mockProbe);
        when(mockThrottlingService.getThrottler(eq(AbstractThrottlingService.API.DEFAULT), any()))
                .thenReturn(mockThrottler);
        when(mockThrottlingService.getThrottler(eq(AbstractThrottlingService.API.LOGIN), any()))
                .thenReturn(mockThrottler);
    }
}
