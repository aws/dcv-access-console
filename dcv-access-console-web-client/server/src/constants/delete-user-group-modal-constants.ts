// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

export const DELETE_USER_GROUP_MODAL_CONSTANTS = {
    DELETE_GROUP_BUTTON_TEXT: "Delete",
    CANCEL_BUTTON_TEXT: "Cancel",

    MODAL_HEADER_FN: (userGroupDisplayName: string | undefined) => {
        return `Delete \"${userGroupDisplayName}\"`
    },
    MODAL_BODY: "Deleting this user group cannot be undone. This deletes the user group, but not the users themselves."
}