package authserver.throttling;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@AllArgsConstructor
public class ThrottlingFilter extends OncePerRequestFilter {
    private static final String LOGIN_URI = "/login";
    public static final String THROTTLE_MESSAGE = "Too many requests";
    private static final long NANOS_TO_SECONDS = 1_000_000_000;
    private AbstractThrottlingService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requesterIp = request.getLocalAddr();
        AbstractThrottlingService.API api;
        log.info("request.getRequestURI(): {}", request.getRequestURI());
        if (LOGIN_URI.equals(request.getRequestURI())) {
            api = AbstractThrottlingService.API.LOGIN;
        } else {
            api = AbstractThrottlingService.API.DEFAULT;
        }
        AbstractConsumptionProbe probe =
                rateLimiterService.getThrottler(api, requesterIp).tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            log.debug("RequesterIp: {} not throttled, remaining tokens: {}", requesterIp, probe.getRemainingTokens());
            filterChain.doFilter(request, response);
            return;
        }
        log.debug(
                "RequesterIp: {} throttled, time to refill in seconds: {}",
                requesterIp,
                probe.getNanosToWaitForRefill() / NANOS_TO_SECONDS);
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), THROTTLE_MESSAGE);
    }
}
