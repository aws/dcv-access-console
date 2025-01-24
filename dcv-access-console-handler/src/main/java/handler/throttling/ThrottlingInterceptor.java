package handler.throttling;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class ThrottlingInterceptor implements HandlerInterceptor {
    public static final String THROTTLE_MESSAGE = "Too many requests";
    private static final long NANOS_TO_SECONDS = 1_000_000_000;
    private AbstractThrottlingService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler)
            throws Exception {
        String requesterIp = request.getLocalAddr();

        AbstractConsumptionProbe probe = rateLimiterService.getThrottler(requesterIp).tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            log.debug("RequesterIp: {} not throttled, remaining tokens: {}", requesterIp, probe.getRemainingTokens());
            return true;
        }

        log.debug("RequesterIp: {} throttled, time to refill in seconds: {}", requesterIp,
                probe.getNanosToWaitForRefill() / NANOS_TO_SECONDS);
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), THROTTLE_MESSAGE);
        return false;
    }
}
