package handler.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import handler.throttling.ThrottlingInterceptor;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.AllArgsConstructor;

@Configuration
@EnableCaching
@AllArgsConstructor
public class ThrottlingConfig implements WebMvcConfigurer {
    ThrottlingInterceptor throttlingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(throttlingInterceptor).addPathPatterns("/**");
    }

    @Bean
    public Caffeine caffeineConfig(@Value("${throttling-cache-max-size:1000}") long maxSize,
            @Value("${throttling-cache-max-time-minutes:20}") long maxTime) {
        return Caffeine.newBuilder().maximumSize(maxSize).expireAfterWrite(maxTime, TimeUnit.MINUTES);
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
