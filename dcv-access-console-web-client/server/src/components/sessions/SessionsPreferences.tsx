// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {CollectionPreferencesProps} from "@cloudscape-design/components";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import {SESSIONS_TABLE_COLUMN_DEFINITIONS} from "@/components/sessions/sessions-table/SessionsTableColumnDefinitions";

export const SESSION_CARDS_DEFAULT_PREFERENCES: CollectionPreferencesProps.Preferences = {
    pageSize: 20,
    custom: "cards"
}

const DEFAULT_DISPLAY_COLUMNS = [SESSIONS_CONSTANTS.NAME_ID, SESSIONS_CONSTANTS.STATUS_ID, SESSIONS_CONSTANTS.OWNER_ID]

export const CONTENT_DISPLAY_OPTIONS: ReadonlyArray<CollectionPreferencesProps.ContentDisplayOption> = SESSIONS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        label: column.header
    }
})

const DEFAULT_CONTENT_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = SESSIONS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: DEFAULT_DISPLAY_COLUMNS.includes(column.id!)
    }
})

export const SESSIONS_TABLE_DEFAULT_PREFERENCES: CollectionPreferencesProps.Preferences = {
    contentDisplay: DEFAULT_CONTENT_DISPLAY,
    pageSize: 20,
    custom:  "table"
}