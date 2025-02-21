package com.amazon.dcv.sm.ui.authserver.throttling.bucket4j;

import com.amazon.dcv.sm.ui.authserver.throttling.AbstractConsumptionProbe;
import io.github.bucket4j.ConsumptionProbe;

public class Bucket4jConsumptionProbe implements AbstractConsumptionProbe {
    private final ConsumptionProbe probe;

    public Bucket4jConsumptionProbe(ConsumptionProbe probe) {
        this.probe = probe;
    }

    @Override
    public long getRemainingTokens() {
        return probe.getRemainingTokens();
    }

    @Override
    public long getNanosToWaitForRefill() {
        return probe.getNanosToWaitForRefill();
    }

    @Override
    public boolean isConsumed() {
        return probe.isConsumed();
    }
}
