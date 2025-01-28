// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.errors;

public enum DescribeUserGroupsErrors implements HandlerErrorMessage {
    DESCRIBE_USER_GROUPS_DEFAULT_MESSAGE("Error while describing user groups");



    /**
     * The enum description.
     */
    private final String mDescription;
    DescribeUserGroupsErrors(final String description) {
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
