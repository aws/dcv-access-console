// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {FormField, Multiselect, SpaceBetween} from "@cloudscape-design/components";
import {OptionDefinition} from "@cloudscape-design/components/internal/components/option/interfaces";
import {useRef, useState} from "react";
import {DescribeSessionTemplatesResponse} from "@/generated-src/client";
import {DropdownStatusProps} from "@cloudscape-design/components/internal/components/dropdown-status/interfaces";
import {OptionsLoadItemsDetail} from "@cloudscape-design/components/internal/components/dropdown/interfaces";

export type SelectStateType = {
    options: OptionDefinition[],
    status: DropdownStatusProps.StatusType,
    errorMessage?: string
}

export type EditUserGroupSessionTemplatesProps = {
    handleLoadItems: ({filteringText, firstPage, samePage}: OptionsLoadItemsDetail) =>  void,
    sessionTemplatesSharedWithGroup: OptionDefinition[],
    setSessionTemplatesSharedWithGroup: (sessionTemplates: OptionDefinition[]) => void,
    selectSessionTemplatesState: SelectStateType,
    formConstants: any
}

export default function EditUserGroupSessionTemplates(
    {
        handleLoadItems,
        sessionTemplatesSharedWithGroup,
        setSessionTemplatesSharedWithGroup,
        formConstants,
        selectSessionTemplatesState,
    }: EditUserGroupSessionTemplatesProps) {

    return (
        <FormField
            stretch
        >
            <SpaceBetween size={"xs"}>
                <Multiselect
                    onChange={({detail}) => {
                        // @ts-ignore
                        setSessionTemplatesSharedWithGroup(detail.selectedOptions)
                    }}
                    placeholder={formConstants.CHOOSE_TEMPLATE_PLACEHOLDER}
                    loadingText={formConstants.LOADING_SESSION_TEMPLATES_TEXT}
                    statusType={selectSessionTemplatesState.status}
                    errorText={formConstants.LOADING_SESSION_TEMPLATES_ERROR_TEXT + selectSessionTemplatesState?.errorMessage}
                    filteringType={"manual"}
                    empty={formConstants.NO_SESSION_TEMPLATES_TEXT}
                    options={selectSessionTemplatesState.options}
                    selectedOptions={sessionTemplatesSharedWithGroup}
                    onLoadItems={event => handleLoadItems(event.detail)}
                />
            </SpaceBetween>
        </FormField>
    )
}