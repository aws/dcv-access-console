// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.exceptions;

public class BadRequestException extends RuntimeException {
    private final String message;

    public BadRequestException(String err) {
        super(err);
        message = err;
    }

    public BadRequestException(Throwable err) {
        super(err);
        message = err.getMessage();
    }

    public String getMessage() {
        return message;
    }
}