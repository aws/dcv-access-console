// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {PropertyFilterOption} from "@cloudscape-design/collection-hooks";
import * as React from "react";
import {Calendar, DateInput, FormField, TimeInput} from "@cloudscape-design/components";
import {PropertyFilterProps} from "@cloudscape-design/components/property-filter";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import {State} from "@/generated-src/client";

const TIMEZONE_OFFSET_STRING = getOffsetString()

export const SPECIAL_OPERATORS = new Map<string, string>([
    [":", "CONTAINS"],
    ["!:", "NOT_CONTAINS"]]
)

export const VALUE_TO_LABELS = new Map<string, string>([
    [State.Ready, SESSIONS_CONSTANTS.AVAILABLE],
    [State.Creating, SESSIONS_CONSTANTS.CREATING],
    [State.Unknown, SESSIONS_CONSTANTS.UNKNOWN],
    [State.Deleting, SESSIONS_CONSTANTS.CLOSING],
    [State.Deleted, SESSIONS_CONSTANTS.CLOSED],

])
export function getEnumFilteringOptions(tokenJson: any): Map<string, ReadonlyArray<PropertyFilterProps.FilteringOption>> {
    let propertyKeys = new Map<string, Array<PropertyFilterOption>>()
    for (let tokenJsonKey in tokenJson) {
        if (tokenJson[tokenJsonKey].Value instanceof Array) {
            tokenJson[tokenJsonKey].Value.forEach((value: string) => {
                propertyKeys[tokenJsonKey] = propertyKeys[tokenJsonKey] || []
                propertyKeys[tokenJsonKey].push({
                    propertyKey: tokenJsonKey,
                    value: value,
                    label: VALUE_TO_LABELS.has(value) ? VALUE_TO_LABELS.get(value) : value
                })
            })
        }
    }
    return propertyKeys
}

export function getFilteringProperties(tokenJson: any, tokenNamesMap: Record<string, string>, tokenNameGroupMap: Record<string, string>) {
    let filterProperties = [];
    for (let tokenJsonKey in tokenJson) {
        if(!(tokenJsonKey in tokenNamesMap)) {
            continue
        }

        let tokenObj = {
            "key": tokenJsonKey,
            "operators": tokenJson[tokenJsonKey]["Operators"],
            "propertyLabel": tokenNamesMap[tokenJsonKey],
            "groupValuesLabel": tokenNameGroupMap[tokenJsonKey]
        }

        if (tokenJson[tokenJsonKey].Value === "offset-date-time") {
            tokenObj["operators"] = tokenObj["operators"].map((operator: string) => ({
                operator,
                form: dateForm,
                format: (tokenValue) => tokenValue,
                match: "date",
            }))
        }

        filterProperties.push(tokenObj);
    }
    return filterProperties
}

export function dateForm({value, onChange}: { value: string, onChange: (value: string) => {} }) {
    return (
        <div className="date-form">
            <FormField>
                <TimeInput
                    onChange={event => {
                        onChange((value?.split("T")[0] || "") + "T" + event.detail.value + TIMEZONE_OFFSET_STRING)
                    }}
                    value={value?.split("T")[1] || ""}
                    format="hh:mm:ss"
                    placeholder="hh:mm:ss"
                />
                <DateInput
                    value={value?.split("T")[0] || ""}
                    onChange={event => {
                        onChange(event.detail.value + "T" + (value?.split("T")[1] || "00:00:00" + TIMEZONE_OFFSET_STRING))
                    }}
                    placeholder="YYYY/MM/DD"
                />
            </FormField>
            <Calendar
                value={value?.split("T")[0] || ""}
                onChange={event => {
                    onChange(event.detail.value + "T" + (value?.split("T")[1] || "00:00:00" + TIMEZONE_OFFSET_STRING))
                }}
            />
        </div>
    )
}

export function getOffsetString() {
    let offset = new Date().getTimezoneOffset();

    let offsetSign = offset >= 0 ? "+" : "-"

    let offsetMinutes = Math.abs(offset % 60)
    let offsetMinutesString = String(offsetMinutes).padStart(2, '0')

    let offsetHours = Math.abs(Math.round(offset / 60))
    let offsetHoursString = String(offsetHours).padStart(2, '0')

    return offsetSign + offsetHoursString + ":" + offsetMinutesString
}

export const reduceFieldValues = (object: any, fieldName: string) => {
    return fieldName.split('.').reduce((acc, property) => {
        if (acc) {
            if (acc[property] instanceof Array) {
                return acc[property]
            }
            return acc[property]
        }
        return null
    }, object)
}

export const getFieldValues = (object: any, fieldName: string | string[]): string[] => {
    if(fieldName instanceof Array) {
        const list = reduceFieldValues(object, fieldName[0]) as []
        return list.map(item => {return reduceFieldValues(item, fieldName[1])})
    }
    return [reduceFieldValues(object, fieldName as string)]
}

export const getValueOrUnknown = (value: any): string => {
    return value ? value : SERVER_DETAILS_CONSTANTS.UNKNOWN
}
