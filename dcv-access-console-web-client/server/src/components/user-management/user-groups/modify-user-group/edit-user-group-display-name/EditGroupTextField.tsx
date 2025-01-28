// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {FormField, Input} from "@cloudscape-design/components";
import {EDIT_USER_GROUP_FORM_CONSTANTS} from "@/constants/edit-user-group-form-constants";
import * as React from "react";

export type EditUserGroupDisplayNameProps = {
    text: string,
    setText: (displayName: string) => void,
    label: string,
    descriptionText: string,
    placeholderText: string,
    disabled: boolean,
    errorText?: string,
}
export default function EditGroupTextField({
    text,
    setText,
    label,
    descriptionText,
    placeholderText,
    disabled,
    errorText,
}: EditUserGroupDisplayNameProps) {

    return (
        <FormField
            description={descriptionText}
            label={label}
            errorText={errorText}
            >
            <Input
                value={text}
                onChange={event => {
                    setText(event.detail.value)
                }}
                placeholder={placeholderText}
                disabled={disabled}
            />
        </FormField>
    )
}