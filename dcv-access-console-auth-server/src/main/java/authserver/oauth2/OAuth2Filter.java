package authserver.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@AllArgsConstructor
public class OAuth2Filter extends OncePerRequestFilter {
    private static final String AUTHORIZE_URL = "/oauth2/authorize";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        log.debug("request.getRequestURI(): {}", request.getRequestURI());
        return !AUTHORIZE_URL.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // Check to make sure it has the nonce and the state parameters
        if (!request.getParameterMap().containsKey(OAuth2ParameterNames.STATE)) {
            log.debug("Request does not contain {} parameter, rejecting", OAuth2ParameterNames.STATE);
            response.sendError(
                    HttpStatus.BAD_REQUEST.value(),
                    String.format("Request does not contain %s parameter, rejecting", OAuth2ParameterNames.STATE));
            return;
        }
        if (!request.getParameterMap().containsKey(OidcParameterNames.NONCE)) {
            log.debug("Request does not contain {} parameter, rejecting", OidcParameterNames.NONCE);
            response.sendError(
                    HttpStatus.BAD_REQUEST.value(),
                    String.format("Request does not contain %s parameter, rejecting", OidcParameterNames.NONCE));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
