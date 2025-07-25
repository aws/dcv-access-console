package authserver.providers.headerauth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class HeaderBasedAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * This is generated by the IDE, which is used during deserialization to verify that the sender and receiver of a
     * serialized object have loaded classes for that object that are compatible with respect to serialization
     */
    static final long serialVersionUID = -620031746106317047L;

    private final Object principal;

    public HeaderBasedAuthenticationToken(Object principal) {
        super(null);
        this.principal = principal;
        setAuthenticated(false);
    }

    public HeaderBasedAuthenticationToken(Object principal, boolean authenticated) {
        super(null);
        this.principal = principal;
        setAuthenticated(authenticated);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
