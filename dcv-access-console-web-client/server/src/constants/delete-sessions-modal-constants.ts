// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

export const DELETE_SESSIONS_MODAL_CONSTANTS = {
    DELETE_SESSION_BUTTON_TEXT: "Close",
    CANCEL_BUTTON_TEXT: "Cancel",

    MODAL_HEADER_FN: (sessionName: string | undefined) => {
        return `Close \"${sessionName}\" session`
    },
    MODAL_BODY: "Closing a session cannot be undone. All work saved locally will be lost. Closing a session does not shut down the underlying host server."
}