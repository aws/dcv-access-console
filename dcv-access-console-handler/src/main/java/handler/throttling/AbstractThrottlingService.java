// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.throttling;

public interface AbstractThrottlingService {
    AbstractThrottler getThrottler(String id);
}
