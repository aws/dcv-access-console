// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum PublishSessionTemplateErrors implements HandlerErrorMessage {
    PUBLISH_SESSION_TEMPLATE_NO_IDS("Session Template IDs are required"),
    USER_UNAUTHORIZED_TO_PUBLISH_SESSION_TEMPLATE("User not authorized to publish this session template"),
    PUBLISH_SESSION_TEMPLATE_DEFAULT_MESSAGE("Error while publishing session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    PublishSessionTemplateErrors(final String description) {
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
