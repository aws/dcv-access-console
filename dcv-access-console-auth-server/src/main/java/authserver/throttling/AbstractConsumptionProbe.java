package authserver.throttling;

public interface AbstractConsumptionProbe {

    long getRemainingTokens();

    long getNanosToWaitForRefill();

    boolean isConsumed();
}
