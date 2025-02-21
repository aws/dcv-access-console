package com.amazon.dcv.sm.ui.authserver.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableCaching
@AllArgsConstructor
public class TokenInvalidationConfig implements WebMvcConfigurer {

    public static final String INITIALIZED_AUTHORIZATIONS = "INITIALIZED_AUTHORIZATIONS";
    public static final String AUTHORIZATIONS = "AUTHORIZATIONS";

    @Bean("TokenCacheManager")
    public CaffeineCacheManager caffeineCacheManager(@Value("${refresh-token-time-to-live:2h}") String maxTime) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.parse("PT" + maxTime).getSeconds(), TimeUnit.SECONDS));
        caffeineCacheManager.setCacheNames(List.of(INITIALIZED_AUTHORIZATIONS, AUTHORIZATIONS));
        return caffeineCacheManager;
    }
}
