// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.config;

import handler.authorization.SessionManagerAuthorizationManager;
import handler.utils.Slf4jMDCFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class AuthenticationConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SessionManagerAuthorizationManager authorizationManager) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                    .anyRequest().access(authorizationManager)
                )
                .csrf(AbstractHttpConfigurer::disable) // CSRF is not needed because this is not serving any webpages
                .cors(Customizer.withDefaults())
                .addFilterBefore(new Slf4jMDCFilter(), AuthorizationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
