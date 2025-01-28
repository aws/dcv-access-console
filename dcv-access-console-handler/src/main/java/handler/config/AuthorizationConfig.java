// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.config;

import com.cedarpolicy.BasicAuthorizationEngine;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AuthorizationConfig {

    @Bean
    BasicAuthorizationEngine provideCedarBasicAuthorizationEngine() {
        return new BasicAuthorizationEngine();
    }

    @Bean
    ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    ObjectWriter provideObjectMapperWriter() {
        return provideObjectMapper().writer(new DefaultPrettyPrinter());
    }

}
