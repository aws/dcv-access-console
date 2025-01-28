// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {useState} from "react";
import {Multiselect, MultiselectProps} from "@cloudscape-design/components";
import * as React from "react";
import {DropdownStatusProps} from "@cloudscape-design/components/internal/components/dropdown-status";
import {formatFileSize} from "@/components/common/utils/TextUtils";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";

export type SelectFromOptionsProps = {
    selectedItems: (string | number | undefined) [],
    setSelectedItems: (item: any []) => void,
    charactersToSearchAfter: number,
    filteringProperty: string,
    getOptions: (filteringText: string,
                 filteringProperty: string,
                 setStatus: (status: DropdownStatusProps.StatusType) => void,
                 setOptions: (options: MultiselectProps.Option[]) => void) => void,
    loadingText: string,
    emptyText: string,
    continueText: string,
    placeholderText: string,
    errorText: string
}

export default function({selectedItems,
                        setSelectedItems,
                        charactersToSearchAfter,
                        filteringProperty,
                        getOptions,
                        loadingText,
                        emptyText,
                        continueText,
                        placeholderText,
                        errorText}: SelectFromOptionsProps) {
    const [status, setStatus] = useState<DropdownStatusProps.StatusType | undefined>("pending")
    const [options, setOptions] = useState<MultiselectProps.Option[]>([])

    function getLabel(value: string | number | undefined) : string {
        if (filteringProperty === SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_FILTERING_PROPERTY) {
            return formatFileSize(Number(value))
        }
        return value as string
    }

    return <Multiselect
        onChange={({detail}) => {
            setSelectedItems(detail.selectedOptions.map(option => option.value))
        }}
        filteringType={"auto"}
        onLoadItems={async ({detail}) => {
            if (detail.filteringText.length > charactersToSearchAfter) {
                await getOptions(detail.filteringText, filteringProperty, setStatus, setOptions)
            } else {
                setStatus("pending")
                setOptions([])
            }
        }
        }
        selectedOptions={selectedItems ? selectedItems.map(item => {return {value: item, label: getLabel(item)} as MultiselectProps.Option}) : []}
        options={options}
        placeholder={placeholderText}
        empty={(() => {
            return (status === "finished" ? emptyText : continueText)
        })()
        }
        statusType={status}
        errorText={errorText}
        loadingText={loadingText}
    />
}