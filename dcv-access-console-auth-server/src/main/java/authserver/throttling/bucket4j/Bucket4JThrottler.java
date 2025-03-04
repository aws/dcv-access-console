package authserver.throttling.bucket4j;

import authserver.throttling.AbstractConsumptionProbe;
import authserver.throttling.AbstractThrottler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class Bucket4JThrottler implements AbstractThrottler {
    private final Bucket bucket;

    @Override
    public boolean tryConsume(long numTokens) {
        return bucket.tryConsume(numTokens);
    }

    @Override
    public AbstractConsumptionProbe tryConsumeAndReturnRemaining(long numTokens) {
        return new Bucket4jConsumptionProbe(bucket.tryConsumeAndReturnRemaining(numTokens));
    }
}
