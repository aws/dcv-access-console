// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.exceptions;

public class BrokerAuthenticationException extends RuntimeException {
    private final String message;

    public BrokerAuthenticationException(String err) {
        super(err);
        message = err;
    }

    public BrokerAuthenticationException(Throwable err) {
        super(err);
        message = err.getMessage();
    }

    public String getMessage() {
        return message;
    }
}