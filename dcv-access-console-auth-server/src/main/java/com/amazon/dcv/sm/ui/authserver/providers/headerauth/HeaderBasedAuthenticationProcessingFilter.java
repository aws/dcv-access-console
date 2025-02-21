package com.amazon.dcv.sm.ui.authserver.providers.headerauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class HeaderBasedAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/login");

    private final String headerName;

    public HeaderBasedAuthenticationProcessingFilter(
            String headerName,
            AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this(headerName, DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager, sessionAuthenticationStrategy);
    }

    public HeaderBasedAuthenticationProcessingFilter(
            String headerName,
            RequestMatcher matcher,
            AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        super(matcher, authenticationManager);
        this.headerName = headerName;
        setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
        setSecurityContextRepository(new HttpSessionSecurityContextRepository());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String headerValue = request.getHeader(this.headerName);
        HeaderBasedAuthenticationToken authRequest = new HeaderBasedAuthenticationToken(headerValue, false);
        authRequest.setDetails(request);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    public String getHeaderName() {
        return headerName;
    }
}
