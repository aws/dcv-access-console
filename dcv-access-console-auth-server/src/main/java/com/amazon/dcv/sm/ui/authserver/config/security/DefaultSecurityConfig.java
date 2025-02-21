package com.amazon.dcv.sm.ui.authserver.config.security;

import com.amazon.dcv.sm.ui.authserver.logging.Slf4jMDCFilter;
import com.amazon.dcv.sm.ui.authserver.oauth2.OAuth2Filter;
import com.amazon.dcv.sm.ui.authserver.security.SecurityFilter;
import com.amazon.dcv.sm.ui.authserver.throttling.ThrottlingFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Configuration
@AllArgsConstructor
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class DefaultSecurityConfig implements WebMvcConfigurer {

    private ThrottlingFilter throttlingFilter;
    private Slf4jMDCFilter mdcFilter;

    private OAuth2Filter oAuth2Filter;
    private SecurityFilter securityFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        // Add throttling filter, needs to be added before the auth filter
        http.addFilterBefore(throttlingFilter, LogoutFilter.class);
        http.addFilterBefore(mdcFilter, LogoutFilter.class);
        http.addFilterAfter(oAuth2Filter, ThrottlingFilter.class);
        http.addFilterAfter(securityFilter, OAuth2Filter.class);

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults())
                .authorizationEndpoint(authEndpoint ->
                        authEndpoint.authorizationResponseHandler((request, response, authentication) -> {

                            // This copied from OAuth2AuthorizationEndpointFilter::sendAuthorizationResponse
                            OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
                                    (OAuth2AuthorizationCodeRequestAuthenticationToken) authentication;
                            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(Objects.requireNonNull(
                                            authorizationCodeRequestAuthentication.getRedirectUri()))
                                    .queryParam(
                                            OAuth2ParameterNames.CODE,
                                            Objects.requireNonNull(
                                                            authorizationCodeRequestAuthentication
                                                                    .getAuthorizationCode())
                                                    .getTokenValue());
                            if (StringUtils.hasText(authorizationCodeRequestAuthentication.getState())) {
                                uriBuilder.queryParam(
                                        OAuth2ParameterNames.STATE,
                                        UriUtils.encode(
                                                authorizationCodeRequestAuthentication.getState(),
                                                StandardCharsets.UTF_8));
                            }
                            String redirectUri = uriBuilder
                                    .build(true)
                                    .toUriString(); // build(true) -> Components are explicitly encoded

                            // Only change is to invalidate the session so that the user has to login again
                            if (request.getSession(false) != null) {
                                request.getSession(false).invalidate();
                            }
                            new DefaultRedirectStrategy().sendRedirect(request, response, redirectUri);
                        })) // Enable OpenID Connect 1.0
        ;
        http
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                // Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        }
    }

    public static HttpSecurity addPublicPagesAndErrors(HttpSecurity http, boolean addLogin) throws Exception {
        http.authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers("/*.svg").permitAll();
                    authorize.requestMatchers("/*.png").permitAll();
                    authorize.requestMatchers("/*.ico").permitAll();
                    authorize.requestMatchers("/_next/**").permitAll();
                    authorize.requestMatchers("/resources/**").permitAll();
                    authorize.requestMatchers("/error").permitAll();
                    authorize.requestMatchers("/*.html").permitAll();
                    authorize.requestMatchers("/").permitAll();
                    if (addLogin) {
                        authorize.requestMatchers("/login").permitAll();
                    }
                })
                .exceptionHandling(exception -> {
                    exception.accessDeniedPage("/404.html");
                });
        return http;
    }
}
