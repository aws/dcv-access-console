// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum DeleteSessionsErrors implements HandlerErrorMessage {
    DELETE_SESSIONS_DEFAULT_MESSAGE("Error while deleting sessions");



    /**
     * The enum description.
     */
    private final String mDescription;
    DeleteSessionsErrors(final String description) {
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
