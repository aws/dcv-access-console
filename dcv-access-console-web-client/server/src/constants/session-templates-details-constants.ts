// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

export const SESSION_TEMPLATES_DETAILS_CONSTANTS = {
    DETAILS: "Details",
    USERS: "Users",
    GROUPS: "Groups",
    REQUIREMENTS: "Additional requirements",

    LOADING_TEXT: "Loading session template",
    ERROR_TEXT: "Error loading session template",
    EMPTY_TEXT: "Select a session template",
    NOT_FOUND: "No session template selected",

    MULTIPLE_SELECTED_TEXT_FN: (numberOfTemplates: number) => {
        return `${numberOfTemplates} templates selected`
    },

    MULTIPLE_SELECTED_BODY: "Select a single template to see its details."
}
