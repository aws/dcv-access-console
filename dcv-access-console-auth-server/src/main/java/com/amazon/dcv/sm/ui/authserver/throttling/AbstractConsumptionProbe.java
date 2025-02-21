package com.amazon.dcv.sm.ui.authserver.throttling;

public interface AbstractConsumptionProbe {

    long getRemainingTokens();

    long getNanosToWaitForRefill();

    boolean isConsumed();
}
