package authserver.providers.headerauth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.ObjectUtils;

public class HeaderAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        HeaderBasedAuthenticationToken authToken = (HeaderBasedAuthenticationToken) authentication;
        if (!(authToken.getPrincipal() instanceof String) || ObjectUtils.isEmpty(authToken.getPrincipal())) {
            throw new AuthenticationServiceException("Header Authentication failed");
        }
        return new HeaderBasedAuthenticationToken(authentication.getPrincipal(), true);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (HeaderBasedAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
