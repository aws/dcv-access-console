// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum DescribeUserGroupsSharedWithSessionTemplateErrors implements HandlerErrorMessage {
    USER_UNAUTHORIZED_ERROR("User is not authorized to describe user groups published to this session template"),
    SESSION_TEMPLATE_ID_NULL("Session Template ID cannot be null"),
    DESCRIBE_USER_GROUPS_SHARED_WITH_SESSION_TEMPLATE_DEFAULT_MESSAGE("Error while describing user groups shared with session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    DescribeUserGroupsSharedWithSessionTemplateErrors(final String description) {
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
