package authserver.throttling.bucket4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import authserver.throttling.AbstractConsumptionProbe;
import authserver.throttling.AbstractThrottler;
import authserver.throttling.AbstractThrottlingService;
import authserver.throttling.ThrottlingFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class Bucket4JThrottlingTest {

    @Autowired
    ThrottlingFilter interceptor;

    @Autowired
    private AbstractThrottlingService throttlingService;

    @Test
    public void testValidRequest() {
        Bucket4JThrottlingService throttling = new Bucket4JThrottlingService(2, 1, 60, 10, 10, 3600);
        AbstractThrottler throttler = throttling.getThrottler(AbstractThrottlingService.API.DEFAULT, "test");

        boolean result = throttler.tryConsume(1);
        AbstractConsumptionProbe resultProbe = throttler.tryConsumeAndReturnRemaining(1);
        assertTrue(result);
        assertTrue(resultProbe.isConsumed());
        assertEquals(0, resultProbe.getRemainingTokens());
        assertEquals(0, resultProbe.getNanosToWaitForRefill());
    }

    @Test
    public void testThrottledRequest() {
        Bucket4JThrottlingService throttling = new Bucket4JThrottlingService(2, 1, 60, 10, 10, 3600);
        AbstractThrottler throttler = throttling.getThrottler(AbstractThrottlingService.API.DEFAULT, "test");
        for (int i = 0; i < 100; i++) {
            throttler.tryConsume(1);
        }
        AbstractConsumptionProbe resultProbe = throttler.tryConsumeAndReturnRemaining(2);
        assertFalse(resultProbe.isConsumed());
        assertEquals(0, resultProbe.getRemainingTokens());
        assertTrue(0L < resultProbe.getNanosToWaitForRefill());
    }

    @Test
    public void testThrottledLoginRequest() {
        Bucket4JThrottlingService throttling = new Bucket4JThrottlingService(2, 1, 60, 10, 10, 3600);
        AbstractThrottler throttler = throttling.getThrottler(AbstractThrottlingService.API.LOGIN, "test");
        for (int i = 0; i < 11; i++) {
            throttler.tryConsume(1);
        }
        AbstractConsumptionProbe resultProbe = throttler.tryConsumeAndReturnRemaining(1);
        assertFalse(resultProbe.isConsumed());
        assertEquals(0, resultProbe.getRemainingTokens());
        assertTrue(0L < resultProbe.getNanosToWaitForRefill());
    }
}
