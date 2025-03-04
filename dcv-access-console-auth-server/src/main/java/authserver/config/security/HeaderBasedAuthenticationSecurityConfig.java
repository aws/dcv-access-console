package authserver.config.security;

import static authserver.config.security.DefaultSecurityConfig.addPublicPagesAndErrors;

import authserver.providers.headerauth.HeaderAuthenticationProvider;
import authserver.providers.headerauth.HeaderBasedAuthenticationProcessingFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.LazyCsrfTokenRepository;

@Configuration
@ConditionalOnProperty(name = "authentication-header-name")
public class HeaderBasedAuthenticationSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http, HeaderBasedAuthenticationProcessingFilter headerBasedAuthenticationProcessingFilter)
            throws Exception {

        addPublicPagesAndErrors(http, false);
        http.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .addFilterAfter(headerBasedAuthenticationProcessingFilter, LogoutFilter.class);

        return http.build();
    }

    @Bean
    public HeaderBasedAuthenticationProcessingFilter headerBasedAuthenticationProcessingFilter(
            @Value("${authentication-header-name}") String headerName,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            AuthenticationManager authenticationManager) {

        return new HeaderBasedAuthenticationProcessingFilter(
                headerName, authenticationManager, sessionAuthenticationStrategy);
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new CompositeSessionAuthenticationStrategy(Arrays.asList(
                new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(new LazyCsrfTokenRepository(new HttpSessionCsrfTokenRepository()))));
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(new HeaderAuthenticationProvider()));
    }
}
