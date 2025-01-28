// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.exceptions;

public class BrokerClientException extends RuntimeException {
    private final String message;
    public BrokerClientException(Throwable err) {
        super(err);
        message = "Unexpected broker client error: " + err.getMessage();
    }

    public String getMessage() {
        return message;
    }
}