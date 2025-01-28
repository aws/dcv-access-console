// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization;

import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.engines.CedarAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.SystemAction;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SessionManagerAuthorizationManagerTest {

    public final SessionManagerAuthorizationManager sessionManagerAuthorizationManager;
    public final Authentication jwtAuthentication;
    public AbstractAuthorizationEngine authorizationEngine;
    public Supplier<Authentication> supplier;

    public final static String DESCRIBE_SERVERS_URI = "/testPrefix/describeServers";
    public final static SystemAction DESCRIBE_SERVERS_ACTION = SystemAction.describeServers;
    public final static String VIEW_HOST_DETAILS_URI = "/testPrefix/viewServerDetails";
    public final static String ERROR_URI = "/error";
    public final static String FAKE_URI = "/gibberish";
    public final static String USER_NAME = "User";
    public SessionManagerAuthorizationManagerTest() {
        this.authorizationEngine = mock(CedarAuthorizationEngine.class);
        this.sessionManagerAuthorizationManager = new SessionManagerAuthorizationManager(authorizationEngine);
        this.jwtAuthentication = mock(JwtAuthenticationToken.class);
        this.supplier=  mock(Supplier.class);

        ReflectionTestUtils.setField(sessionManagerAuthorizationManager, "handlerPrefix", "/testPrefix");
    }

    @Test
    public void testUserIsAuthorizedForSystemAction() {
        when(this.supplier.get()).thenReturn(this.jwtAuthentication);

        RequestAuthorizationContext requestAuthorizationContext = mock(RequestAuthorizationContext.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(DESCRIBE_SERVERS_URI);
        when(requestAuthorizationContext.getRequest()).thenReturn(request);
        when(this.jwtAuthentication.getName()).thenReturn(USER_NAME);
        when(this.authorizationEngine.isAuthorized(PrincipalType.User, USER_NAME, DESCRIBE_SERVERS_ACTION)).thenReturn(true);

        assertTrue(this.sessionManagerAuthorizationManager.check(supplier, requestAuthorizationContext).isGranted());
    }

    @Test
    public void testUserIsAccessingResourceAction() {
        when(this.supplier.get()).thenReturn(this.jwtAuthentication);
        RequestAuthorizationContext requestAuthorizationContext = mock(RequestAuthorizationContext.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(VIEW_HOST_DETAILS_URI);
        when(requestAuthorizationContext.getRequest()).thenReturn(request);

        assertTrue(this.sessionManagerAuthorizationManager.check(this.supplier, requestAuthorizationContext).isGranted());
    }

    @Test
    public void testErrorIsAllowed() {
        RequestAuthorizationContext requestAuthorizationContext = mock(RequestAuthorizationContext.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(ERROR_URI);
        when(requestAuthorizationContext.getRequest()).thenReturn(request);

        assertTrue(this.sessionManagerAuthorizationManager.check(this.supplier, requestAuthorizationContext).isGranted());
    }

    @Test
    public void testUnknownURI() {
        when(this.supplier.get()).thenReturn(this.jwtAuthentication);

        RequestAuthorizationContext requestAuthorizationContext = mock(RequestAuthorizationContext.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(FAKE_URI);
        when(requestAuthorizationContext.getRequest()).thenReturn(request);
        when(this.jwtAuthentication.getName()).thenReturn(USER_NAME);

        assertFalse(this.sessionManagerAuthorizationManager.check(supplier, requestAuthorizationContext).isGranted());
    }

    @Test
    public void testBadJWT() {
        Authentication badAuthentication = mock(TestingAuthenticationToken.class);

        when(this.supplier.get()).thenReturn(badAuthentication);
        RequestAuthorizationContext requestAuthorizationContext = mock(RequestAuthorizationContext.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(DESCRIBE_SERVERS_URI);
        when(requestAuthorizationContext.getRequest()).thenReturn(request);

        assertFalse(this.sessionManagerAuthorizationManager.check(supplier, requestAuthorizationContext).isGranted());
    }

    @Test
    public void testNoAuthorizationDecision() {
        when(this.supplier.get()).thenReturn(this.jwtAuthentication);

        RequestAuthorizationContext requestAuthorizationContext = mock(RequestAuthorizationContext.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(DESCRIBE_SERVERS_URI);
        when(requestAuthorizationContext.getRequest()).thenReturn(request);
        when(this.jwtAuthentication.getName()).thenReturn(USER_NAME);
        when(this.authorizationEngine.isAuthorized(PrincipalType.User, USER_NAME, DESCRIBE_SERVERS_ACTION)).thenThrow(AuthorizationServiceException.class);

        assertFalse(this.sessionManagerAuthorizationManager.check(supplier, requestAuthorizationContext).isGranted());
    }
}
