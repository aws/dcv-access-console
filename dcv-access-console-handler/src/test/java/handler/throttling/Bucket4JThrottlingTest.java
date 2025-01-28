// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.throttling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import handler.throttling.bucket4j.Bucket4JThrottlingService;

@ExtendWith(MockitoExtension.class)
public class Bucket4JThrottlingTest {

    @Autowired
    ThrottlingInterceptor interceptor;

    @Autowired
    private AbstractThrottlingService throttlingService;

    @Test
    public void testValidRequest() {
        Bucket4JThrottlingService throttling = new Bucket4JThrottlingService(2, 1, 60);
        AbstractThrottler throttler = throttling.getThrottler("test");

        boolean result = throttler.tryConsume(1);
        AbstractConsumptionProbe resultProbe = throttler.tryConsumeAndReturnRemaining(1);
        assertTrue(result);
        assertTrue(resultProbe.isConsumed());
        assertEquals(0, resultProbe.getRemainingTokens());
        assertEquals(0, resultProbe.getNanosToWaitForRefill());
    }

    @Test
    public void testThrottledRequest() {
        Bucket4JThrottlingService throttling = new Bucket4JThrottlingService(2, 1, 60);
        AbstractThrottler throttler = throttling.getThrottler("test");
        for (int i = 0; i < 100; i++) {
            throttler.tryConsume(1);
        }
        AbstractConsumptionProbe resultProbe = throttler.tryConsumeAndReturnRemaining(2);
        assertFalse(resultProbe.isConsumed());
        assertEquals(0, resultProbe.getRemainingTokens());
        assertTrue(0L < resultProbe.getNanosToWaitForRefill());

    }
}
