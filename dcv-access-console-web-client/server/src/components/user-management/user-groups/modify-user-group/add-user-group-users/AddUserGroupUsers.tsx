// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Multiselect, SpaceBetween} from "@cloudscape-design/components";
import * as React from "react";
import {OptionDefinition} from "@cloudscape-design/components/internal/components/option/interfaces";
import {OptionsLoadItemsDetail} from "@cloudscape-design/components/internal/components/dropdown/interfaces";
import {
    SelectStateType
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-session-templates/EditUserGroupSessionTemplates";
export type FormConstants = {
    PLACEHOLDER: string,
    EMPTY: string,
    ERROR: string,
    LOADING: string
}
export type AddUserGroupUsersProps = {
    handleLoadItems: ({filteringText, firstPage, samePage}: OptionsLoadItemsDetail) =>  void,
    selectItemsState: SelectStateType,
    itemsToAdd: OptionDefinition[],
    setItemsToAdd: (sessionTemplates: OptionDefinition[]) => void,
    formConstants: FormConstants,
    addHorizontalLine?: boolean
}
export default function AddUserGroupUsers(
    {
        handleLoadItems,
        selectItemsState,
        itemsToAdd,
        setItemsToAdd,
        formConstants,
        addHorizontalLine = true
    }: AddUserGroupUsersProps) {

    return <SpaceBetween size={"m"}>
        <Multiselect
            onChange={({detail}) => {
                // @ts-ignore
                setItemsToAdd(detail.selectedOptions)
            }}
            filteringType={"manual"}
            onLoadItems={event => handleLoadItems(event.detail)}
            selectedOptions={itemsToAdd}
            options={selectItemsState.options}
                placeholder={formConstants.PLACEHOLDER}
                empty={formConstants.EMPTY}
                statusType={selectItemsState.status}
                errorText={ formConstants.ERROR + selectItemsState.errorMessage}
                loadingText={formConstants.LOADING}
            />
        {addHorizontalLine? <hr/> : <></>}
        </SpaceBetween>
}
