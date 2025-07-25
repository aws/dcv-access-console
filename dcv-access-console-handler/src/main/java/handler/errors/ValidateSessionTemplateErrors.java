// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum ValidateSessionTemplateErrors implements HandlerErrorMessage {
    VALIDATE_SESSION_TEMPLATE_DEFAULT_MESSAGE("Error while validating session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    ValidateSessionTemplateErrors(final String description) {
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
