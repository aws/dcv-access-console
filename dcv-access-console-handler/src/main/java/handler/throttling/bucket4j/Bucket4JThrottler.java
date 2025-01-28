// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.throttling.bucket4j;

import io.github.bucket4j.Bucket;

import handler.throttling.AbstractConsumptionProbe;
import handler.throttling.AbstractThrottler;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Bucket4JThrottler implements AbstractThrottler {
    private final Bucket bucket;
    @Override
    public boolean tryConsume(long numTokens) {
        return bucket.tryConsume(numTokens);
    }

    @Override
    public AbstractConsumptionProbe tryConsumeAndReturnRemaining(long numTokens) {
        return new Bucket4jConsumptionProbe(bucket.tryConsumeAndReturnRemaining(numTokens));
    }
}
