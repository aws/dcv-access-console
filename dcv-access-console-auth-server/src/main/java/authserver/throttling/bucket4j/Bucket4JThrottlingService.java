package authserver.throttling.bucket4j;

import authserver.throttling.AbstractThrottler;
import authserver.throttling.AbstractThrottlingService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@NoArgsConstructor
@AllArgsConstructor
public class Bucket4JThrottlingService implements AbstractThrottlingService {

    @Value("${throttling-burst}")
    private long burst;

    @Value("${throttling-refill}")
    private long refill;

    @Value("${throttling-period-in-seconds}")
    private long periodInSeconds;

    @Value("${throttling-login-burst}")
    private long loginBurst;

    @Value("${throttling-login-refill}")
    private long loginRefill;

    @Value("${throttling-login-period-in-seconds}")
    private long loginPeriodInSeconds;

    @Override
    @Cacheable("throttling-cache")
    public AbstractThrottler getThrottler(String id) {
        // This is cached using spring-cache. Each subsequent call to this method will return the same throttler.
        return new Bucket4JThrottler(Bucket.builder()
                .addLimit(Bandwidth.classic(burst, Refill.intervally(refill, Duration.ofSeconds(periodInSeconds))))
                .build());
    }

    @Override
    @Cacheable("throttling-cache")
    public AbstractThrottler getThrottler(API api, String id) {
        if (API.LOGIN.equals(api)) {
            return new Bucket4JThrottler(Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            loginBurst, Refill.intervally(loginRefill, Duration.ofSeconds(loginPeriodInSeconds))))
                    .build());
        }
        return getThrottler(id);
    }
}
