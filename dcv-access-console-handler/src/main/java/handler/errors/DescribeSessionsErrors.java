// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;
public enum DescribeSessionsErrors implements HandlerErrorMessage {
    DESCRIBE_SESSIONS_DEFAULT_MESSAGE("Error while describing sessions");


    /**
     * The enum description.
     */
    private final String mDescription;
    DescribeSessionsErrors(final String description) {
        this.mDescription = description;
    }

    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() {
        return this.mDescription;
    }
}
