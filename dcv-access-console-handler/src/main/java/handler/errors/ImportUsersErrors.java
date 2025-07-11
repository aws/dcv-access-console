// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum ImportUsersErrors implements HandlerErrorMessage {
    IMPORT_USERS_DEFAULT_MESSAGE("Error while importing users");


    /**
     * The enum description.
     */
    private final String mDescription;
    ImportUsersErrors(final String description) {
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
