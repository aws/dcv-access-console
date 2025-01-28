// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {CollectionPreferencesProps} from "@cloudscape-design/components";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {SEVERS_TABLE_COLUMN_DEFINITIONS} from "@/components/servers/servers-table/ServersTableColumnDefinitions";

const DEFAULT_DISPLAY_COLUMNS = [SERVERS_TABLE_CONSTANTS.HOSTNAME_ID, SERVERS_TABLE_CONSTANTS.OS_ID, SERVERS_TABLE_CONSTANTS.AVAILABILITY_ID, SERVERS_TABLE_CONSTANTS.MEMORY_TOTAL_BYTES_ID, SERVERS_TABLE_CONSTANTS.CPU_INFO_ID]

export const CONTENT_DISPLAY_OPTIONS: ReadonlyArray<CollectionPreferencesProps.ContentDisplayOption> = SEVERS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        label: column.header
    }
})

const DEFAULT_CONTENT_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = SEVERS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: DEFAULT_DISPLAY_COLUMNS.includes(column.id!)
    }
})

export const ALL_CONTENT_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = SEVERS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: true
    }
})

export const DEFAULT_PREFERENCES: CollectionPreferencesProps.Preferences = {
    contentDisplay: DEFAULT_CONTENT_DISPLAY,
    pageSize: 20,
}

export const DEFAULT_PAGE_SIZE_PREFERENCES: CollectionPreferencesProps.PageSizePreference = {
    title: "Page size",
    options: [
        { value: 5, label: "5 resources" },
        { value: 10, label: "10 resources" },
        { value: 20, label: "20 resources" },
        { value: 50, label: "50 resources" },
        { value: 100, label: "100 resources" }
    ]
}
