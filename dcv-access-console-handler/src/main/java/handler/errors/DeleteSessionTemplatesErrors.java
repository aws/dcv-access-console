// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum DeleteSessionTemplatesErrors implements HandlerErrorMessage {
    EMPTY_IDS_ERROR("Request must include a list of IDs to delete"),
    DELETE_SESSION_TEMPLATES_DEFAULT_MESSAGE("Error while deleting session template(s)");


    /**
     * The enum description.
     */
    private final String mDescription;
    DeleteSessionTemplatesErrors(final String description) {
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
