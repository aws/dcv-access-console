package handler.authorization;

import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.SystemAction;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManagerAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final static String SLASH = "/";
    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${default-role}")
    private String defaultRoleUUID;

    @Value("${request-prefix:smuihandler}")
    private String handlerPrefix;

    private static final Set<String> EXCEPTIONS = new HashSet<>(List.of(new String[]{
            "error"
    }));

    @Override
    public AuthorizationDecision check(Supplier authentication, RequestAuthorizationContext context) {
        // Get the action from the URI, and ensure it's not a resource action or an error
        String action = context.getRequest().getRequestURI();
        String[] splitString = action.split(SLASH);
        if (splitString.length >= 3) {
            if (StringUtils.startsWith(action, handlerPrefix)) {
                action = splitString[2];
            } else {
                log.warn("Request URI {} did not start with expected prefix {}. This is probably an issue with the WebClient configuration", action, handlerPrefix);
            }
        } else {
            log.warn("Unable to get action for request {}", action);
        }
        if ((   (splitString.length > 1 && EXCEPTIONS.contains(splitString[1])) ||
                (splitString.length > 2 && EXCEPTIONS.contains(splitString[2])))
                || EnumUtils.isValidEnum(ResourceAction.class, action)) {
            log.info("{} action is either ResourceAction or listed as exception. Allowing...", action);
            return new AuthorizationDecision(true);
        }

        // Get the principal from the JWT
        Object auth = authentication.get();
        if (!(auth instanceof JwtAuthenticationToken)) {
            log.warn("Unable to retrieve JWT from authentication {}", authentication.get());
            return new AuthorizationDecision(false);
        }
        String principalUUID = ((JwtAuthenticationToken) authentication.get()).getName();

        // Ensure the action is a valid system action
        if (!EnumUtils.isValidEnum(SystemAction.class, action)) {
            log.warn("Unable to find action {}", action);
            return new AuthorizationDecision(false);
        }
        SystemAction systemAction = SystemAction.valueOf(action);

        log.info("Checking if principal {} is authorized to take action {}", principalUUID, action);
        try {
            if (authorizationEngine.addUserWithPersistence(principalUUID)) {
                log.info("New user {} logged in for the first time. Adding default role {}", principalUUID, defaultRoleUUID);
            }
            // Check if the authorization engine authorizes the request
            return new AuthorizationDecision(authorizationEngine.isAuthorized(PrincipalType.User, principalUUID, systemAction));
        } catch (AuthorizationServiceException ase) {
            log.warn("Unable to make authorization decision: ", ase);
        }
        return new AuthorizationDecision(false);
    }
}
