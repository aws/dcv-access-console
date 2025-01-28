// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {useState} from "react";
import Table from "@cloudscape-design/components/table";
import Box from "@cloudscape-design/components/box";
import {SEVERS_TABLE_COLUMN_DEFINITIONS,} from "@/components/servers/servers-table/ServersTableColumnDefinitions";

import {Server} from "@/generated-src/client";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {CancelableEventHandler, NonCancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {CollectionPreferencesProps, SpaceBetween, TableProps,} from "@cloudscape-design/components";
import Button from "@cloudscape-design/components/button";
import {ButtonProps} from "@cloudscape-design/components/button/interfaces";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {DataAccessServiceParams, DataAccessServiceResult} from "@/components/common/hooks/DataAccessServiceHooks";
import {CONTENT_DISPLAY_OPTIONS} from "@/components/servers/servers-table/ServersTableColumnPreferences";

export type ServersTableProps = {
    selectedServer: Server | undefined,
    onSelectionChange: NonCancelableEventHandler<TableProps.SelectionChangeDetail<Server>>,
    preferences: CollectionPreferencesProps.Preferences,
    setPreferences: (detail: CollectionPreferencesProps.Preferences) => void,
    filter: React.ReactNode
    buttonFunction?: CancelableEventHandler<ButtonProps.ClickDetail>
    infoLinkFollow: CancelableEventHandler<LinkProps.FollowDetail>,
    resetPaginationKey: string,
    dataAccessServiceFunction: (params: DataAccessServiceParams<Server>) => DataAccessServiceResult<Server>
    query: PropertyFilterQuery
}

export default function
    ServersTable({
                     selectedServer,
                     onSelectionChange,
                     preferences,
                     setPreferences,
                     filter,
                     buttonFunction,
                     infoLinkFollow,
                     resetPaginationKey,
                     dataAccessServiceFunction,
                     query = { tokens: [], operation: 'and' }
                 }: ServersTableProps) {
    const [refreshKey, setRefreshKey] = useState("")

    const header = <ConsoleHeader
        headerDescription={SERVERS_TABLE_CONSTANTS.HEADER_DESCRIPTION}
        headerTitle={SERVERS_TABLE_CONSTANTS.HEADER_TEXT}
        infoLinkFollow={infoLinkFollow}
        infoLinkLabel={GLOBAL_CONSTANTS.INFO_LABEL}
        actions={<SpaceBetween direction="horizontal" size="xs">
           <Button disabled={!selectedServer} onClick={buttonFunction || (() => {
           })}>{SERVERS_TABLE_CONSTANTS.BUTTON_TEXT}</Button>
        </SpaceBetween>}
        flashItems={[]}
    />


    const table = <Table
        variant="full-page"
        columnDefinitions={
            SEVERS_TABLE_COLUMN_DEFINITIONS
        }
        columnDisplay={preferences.contentDisplay!}
        items={[]}
        selectedItems={selectedServer ? [selectedServer] as ReadonlyArray<Server> : []}
        onSelectionChange={onSelectionChange}
        trackBy={SERVERS_TABLE_CONSTANTS.ID}
        loadingText={SERVERS_TABLE_CONSTANTS.LOADING_TEXT}
        selectionType="single"
        sortingDisabled={true}  // Delete this line it to reenable sorting (once sorting has been fixed post-pagination)
        filter={filter}
        empty={
            <Box textAlign="center" color="inherit">
                <b>{SERVERS_TABLE_CONSTANTS.EMPTY_TEXT}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        }
    />

    return (
        <TableWithPagination
            table={table}
            header={header}
            defaultSortingColumn={SEVERS_TABLE_COLUMN_DEFINITIONS[1]}
            query={query}
            dataAccessServiceFunction={dataAccessServiceFunction}
            preferences={preferences}
            setPreferences={setPreferences}
            refreshKey={refreshKey}
            setRefreshKey={setRefreshKey}
            resetPaginationKey={resetPaginationKey}
            contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
        />
    );
}
