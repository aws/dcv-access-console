// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum CreateSessionErrors implements HandlerErrorMessage {
    USER_UNAUTHORIZED_SESSION_CREATION_FOR_OTHERS("User is not allowed to create sessions for others"),
    CREATE_SESSION_DEFAULT_MESSAGE("Error while creating session(s)");


    /**
     * The enum description.
     */
    private final String mDescription;
    CreateSessionErrors(final String description) {
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
