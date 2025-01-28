// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import PropertyFilter, {PropertyFilterProps} from "@cloudscape-design/components/property-filter";
import {NonCancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {DropdownStatusProps} from "@cloudscape-design/components/internal/components/dropdown-status";
import {
    getEnumFilteringOptions,
    getFilteringProperties, getFieldValues
} from "@/components/common/utils/SearchUtils";
import {
    DescribeSessionsUIRequestData,
    DescribeSessionTemplatesRequestData,
    DescribeUserGroupsRequestData,
    DescribeUsersRequestData,
    FilterToken,
    FilterTokenOperatorEnum, SessionTemplate, SessionWithPermissions,
    Server, User, UserGroup, DescribeServersUIRequestData
} from "@/generated-src/client";
import {useRef, useState} from "react";
import {FilteringOption} from "@cloudscape-design/components/property-filter/interfaces";
import {FILTER_CONSTANTS, PROPERTY_FILTER_I18N_STRINGS} from "@/constants/generic-search-bar-constants";
import {formatFileSize} from "@/components/common/utils/TextUtils";

export type DescribeResponse = {
    objects:  Array<SessionWithPermissions | Server | SessionTemplate | User | UserGroup>
    nextToken: string
}

export type FilterBarProps = {
    filteringQuery: PropertyFilterProps.Query
    handlePropertyFilteringChange: NonCancelableEventHandler<PropertyFilterProps.Query>
    searchTokens: {}
    tokenNamesMap: {}
    tokenNameGroupMap: {}
    searchTokenToId: Map<string, string | string[]>
    filteringPlaceholder: string
    dataAccessServiceFunction:  (request: DescribeSessionsUIRequestData | DescribeServersUIRequestData | DescribeSessionTemplatesRequestData | DescribeUsersRequestData | DescribeUserGroupsRequestData) => Promise<DescribeResponse>
}
const MEMORY_FILTER_KEYS = ["MemoryTotalBytes", "MemoryUsedBytes", "SwapTotalBytes", "SwapUsedBytes"]
export default function FilterBar(props: FilterBarProps) {
    const [nextToken, setNextToken] = useState('')
    const request = useRef({filteringText: '', filteringProperty: ''})
    const [filteringOptions, setFilteringOptions] = useState<ReadonlyArray<PropertyFilterProps.FilteringOption>>([])
    const [status, setStatus] = useState('pending' as DropdownStatusProps.StatusType)
    const FILTERING_PROPERTIES: ReadonlyArray<PropertyFilterProps.FilteringProperty> = getFilteringProperties(props.searchTokens, props.tokenNamesMap, props.tokenNameGroupMap)
    const ENUM_FILTERING_OPTIONS: Map<string, ReadonlyArray<PropertyFilterProps.FilteringOption>> = getEnumFilteringOptions(props.searchTokens)

    const updateFilteringOptions = (filteringText: string, filteringProperty: string, options: Array<FilteringOption>) => {
        if (
            !request.current ||
            request.current.filteringText !== filteringText ||
            request.current.filteringProperty !== filteringProperty
        ) {
            // there is another request in progress, discard the result of this one
            return
        }
        options.forEach(option => {
            if(MEMORY_FILTER_KEYS.includes(option.propertyKey)) {
                option.label = formatFileSize(+option.value)
            }
        })
        setFilteringOptions(options)
    }

    const fetchFilteringOptions = async (filteringText: string, filteringProperty: string) => {
        let objectProperty = props.searchTokenToId.get(filteringProperty)
        if (!objectProperty) {
            console.debug("Property {} not supported for autofill", filteringProperty)
            setStatus(undefined)
            return
        }

        if (filteringProperty in ENUM_FILTERING_OPTIONS) {
            updateFilteringOptions(filteringText, filteringProperty, ENUM_FILTERING_OPTIONS[filteringProperty])
            setStatus('finished')
            return
        }

        if (props.searchTokens[filteringProperty].Value === "boolean") {
            updateFilteringOptions(filteringText, filteringProperty, [
                {
                    propertyKey: filteringProperty,
                    value: "true"
                } as FilteringOption,
                {
                    propertyKey: filteringProperty,
                    value: "false"
                } as FilteringOption])
            setStatus('finished')
            return
        }

        let handlerRequest = {MaxResults: 10, NextToken: nextToken}

        if (props.searchTokens[filteringProperty].Operators?.includes(":")) {
            handlerRequest[filteringProperty] = [{
                Operator: FilterTokenOperatorEnum.Contains,
                Value: filteringText
            } as FilterToken]
        }

        try {
            const handlerResponse: DescribeResponse = await props.dataAccessServiceFunction(handlerRequest)
            let uniqueOptions = new Set(filteringOptions?.map(option => option.value))
            handlerResponse.objects?.forEach(object => {
                const fieldValues = getFieldValues(object, objectProperty!)
                fieldValues.forEach(value => uniqueOptions.add(value.toString()))
            })
            let asyncFilteringOptions: Array<FilteringOption> = []
            uniqueOptions.forEach(option => {
                asyncFilteringOptions.push({
                    propertyKey: filteringProperty,
                    value: option
                } as FilteringOption)
            })

            updateFilteringOptions(filteringText, filteringProperty, asyncFilteringOptions)
            setNextToken(handlerResponse.nextToken)
            setStatus(handlerResponse.nextToken ? 'pending' : 'finished')
        } catch (e) {
            console.log("Error getting the autofill", e)
            setStatus('error')
        }
    }

    const handleLoadItems = async ({detail}: { detail: PropertyFilterProps.LoadItemsDetail }) => {
        request.current = {
            filteringText: detail.filteringText,
            filteringProperty: detail.filteringProperty?.key!
        }

        if (detail.firstPage) {
            setFilteringOptions([])
        }

        setStatus('loading')
        await fetchFilteringOptions(detail.filteringText, detail.filteringProperty?.key)
    }

    return (
        <PropertyFilter
            i18nStrings={PROPERTY_FILTER_I18N_STRINGS}
            filteringProperties={FILTERING_PROPERTIES}
            filteringOptions={filteringOptions}
            query={props.filteringQuery}
            onChange={props.handlePropertyFilteringChange}
            onLoadItems={handleLoadItems}
            filteringStatusType={status}
            expandToViewport={false}
            hideOperations
            disableFreeTextFiltering
            filteringLoadingText={FILTER_CONSTANTS.filteringLoadingText}
            filteringErrorText={FILTER_CONSTANTS.filteringErrorText}
            filteringRecoveryText={FILTER_CONSTANTS.filteringRecoveryText}
            filteringEmpty={FILTER_CONSTANTS.filteringEmpty}
            filteringFinishedText={FILTER_CONSTANTS.filteringFinishedText}
            filteringPlaceholder={props.filteringPlaceholder}
        />
    )
}
