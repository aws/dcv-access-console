package com.amazon.dcv.sm.ui.authserver.config.security;

import static com.amazon.dcv.sm.ui.authserver.config.security.DefaultSecurityConfig.addPublicPagesAndErrors;

import com.amazon.dcv.sm.ui.authserver.providers.pam.PamAuthenticationProvider;
import com.amazon.dcv.sm.ui.authserver.providers.pam.ProcessBuilderProvider;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.server.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${authentication.pam.service-name:}')")
public class PamAuthenticationSecurityConfig implements WebMvcConfigurer {

    @Bean
    @Order(2)
    public SecurityFilterChain pamSecurityChain(HttpSecurity http) throws Exception {
        addPublicPagesAndErrors(http, true);
        http.formLogin(formLogin -> {
                    formLogin.loginPage("/login").loginProcessingUrl("/login").permitAll();
                })
                .csrf(csrf -> {
                    CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler =
                            new CsrfTokenRequestAttributeHandler();
                    csrfTokenRequestAttributeHandler.setCsrfRequestAttributeName(null);
                    csrf.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler);
                    CookieCsrfTokenRepository repo = new CookieCsrfTokenRepository();
                    repo.setCookieCustomizer(customizer -> {
                        customizer.sameSite(Cookie.SameSite.STRICT.attributeValue());
                        customizer.httpOnly(false);
                    });
                    csrf.csrfTokenRepository(repo);
                });

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(PamAuthenticationProvider provider) {
        return new ProviderManager(List.of(provider));
    }

    @Bean
    public ProcessBuilderProvider processBuilderProvider() {
        return new ProcessBuilderProvider();
    }
}
