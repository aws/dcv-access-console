package com.amazon.dcv.sm.ui.authserver.throttling;

public interface AbstractThrottler {

    boolean tryConsume(long numTokens);

    AbstractConsumptionProbe tryConsumeAndReturnRemaining(long numTokens);
}
