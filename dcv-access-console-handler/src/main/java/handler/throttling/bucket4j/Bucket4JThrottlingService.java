// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.throttling.bucket4j;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import handler.throttling.AbstractThrottler;
import handler.throttling.AbstractThrottlingService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@NoArgsConstructor
@AllArgsConstructor
public class Bucket4JThrottlingService implements AbstractThrottlingService {

    @Value("${throttling-burst}")
    private long burst;

    @Value("${throttling-refill}")
    private long refill;

    @Value("${throttling-period-in-seconds}")
    private long periodInSeconds;

    @Override
    @Cacheable("throttling-cache")
    public AbstractThrottler getThrottler(String id) {
        // This is cached using spring-cache. Each subsequent call to this method will return the same throttler.
        return new Bucket4JThrottler(Bucket.builder()
                .addLimit(Bandwidth.classic(burst, Refill.intervally(refill, Duration.ofSeconds(periodInSeconds))))
                .build());
    }
}
