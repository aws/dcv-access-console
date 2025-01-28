// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum CommonErrorsEnum implements HandlerErrorMessage {
    BAD_REQUEST_ERROR("Bad request"),
    BROKER_AUTHENTICATION_ERROR("Broker authentication error");


    /**
     * The enum description.
     */
    private final String mDescription;

    CommonErrorsEnum(final String description) {
        this.mDescription = description;
    }


    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return this.mDescription;
    }
}
