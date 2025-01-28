// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {CollectionPreferencesProps} from "@cloudscape-design/components";
import {
    USER_SESSION_DETAILS_TABLE_COLUMN_DEFINITIONS
} from "@/components/user-management/common/user-session-details/UserSessionDetailsTableColumnDefinitions";
import {USER_GROUP_SESSION_DETAILS_CONSTANTS} from "@/constants/user-group-session-details-constants";

const DEFAULT_DISPLAY_COLUMNS = [USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_NAME_ID, USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_LEVEL_OF_ACCESS_ID, USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_STATUS_ID]

export const CONTENT_DISPLAY_OPTIONS: ReadonlyArray<CollectionPreferencesProps.ContentDisplayOption> = USER_SESSION_DETAILS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        label: column.header
    }
})

const DEFAULT_CONTENT_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = USER_SESSION_DETAILS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: DEFAULT_DISPLAY_COLUMNS.includes(column.id!)
    }
})

export const DEFAULT_PREFERENCES: CollectionPreferencesProps.Preferences = {
    contentDisplay: DEFAULT_CONTENT_DISPLAY,
    pageSize: 10,
}

export const USER_SESSIONS_DEFAULT_PAGE_SIZE_PREFERENCES: CollectionPreferencesProps.PageSizePreference = {
    title: "Page size",
    options: [
        { value: 5, label: "5 resources" },
        { value: 10, label: "10 resources" },
        { value: 20, label: "20 resources" },
        { value: 50, label: "50 resources" },
        { value: 100, label: "100 resources" }
    ]
}