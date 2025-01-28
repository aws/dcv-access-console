// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

export const DELETE_SESSION_TEMPLATES_MODAL_CONSTANTS = {
    DELETE_SESSION_TEMPLATE_BUTTON_TEXT: "Delete",
    CANCEL_BUTTON_TEXT: "Cancel",

    MODAL_HEADER_FN: (sessionTemplateNames?: string[]) => {
        if (sessionTemplateNames?.length === 1) {
            return `Delete session template \"${sessionTemplateNames[0]}\"`
        } else {
            return `Delete ${sessionTemplateNames?.length} session templates?`
        }
    },

    MODAL_BODY_FN: (sessionTemplates?: string[]) => {
        if (sessionTemplates?.length === 1) {
            return "Deleting this session template cannot be undone. Active sessions that were created with this template will not be affected."
        } else {
            return `Deleting these ${sessionTemplates?.length} session templates cannot be undone. Active sessions that were created with these templates will not be affected.`
        }
    }
}