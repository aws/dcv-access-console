// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum CreateSessionTemplateErrors implements HandlerErrorMessage {
    AUTHORIZATION_ENGINE_FAILED_TO_SAVE_TEMPLATE_ERROR("Authorization engine failed to save session template"),
    CREATE_SESSION_TEMPLATE_DEFAULT_MESSAGE("Error while creating the session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    CreateSessionTemplateErrors(final String description) {
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
