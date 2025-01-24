package handler.throttling.bucket4j;


import io.github.bucket4j.ConsumptionProbe;

import handler.throttling.AbstractConsumptionProbe;

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
