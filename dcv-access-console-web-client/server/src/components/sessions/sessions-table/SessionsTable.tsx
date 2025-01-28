// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {ReactNode, useEffect, useState} from "react";
import Table from "@cloudscape-design/components/table";
import {SessionWithPermissions} from "@/generated-src/client";
import {CollectionPreferencesProps, SpaceBetween} from "@cloudscape-design/components";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import {SessionsState} from "@/app/sessions/page";
import DeleteSessionsModal from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import LaunchSessionModal from "@/components/sessions/launch-session-modal/LaunchSessionModal";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import {DataAccessServiceParams, DataAccessServiceResult} from "@/components/common/hooks/DataAccessServiceHooks";
import {CONTENT_DISPLAY_OPTIONS, SESSIONS_TABLE_DEFAULT_PREFERENCES} from "@/components/sessions/SessionsPreferences";
import {SESSIONS_TABLE_COLUMN_DEFINITIONS} from "@/components/sessions/sessions-table/SessionsTableColumnDefinitions";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";

export type SessionsTableProps = {
    getViewForm: (value: string, setValue: (string) => void) => JSX.Element
    setView: (view : string) => void
    deleteItemsKey: string,
    sessionsState: SessionsState
    setSessionsState: (sessionsState: any) => void
    describeSessionsQuery: PropertyFilterQuery
    getFilter: (resetPaginationFunc) => JSX.Element
    createButton: ReactNode
    actions: (session: SessionWithPermissions | undefined) => ReactNode
    connectButton: (session: SessionWithPermissions | undefined) => ReactNode
    deleteModal: DeleteSessionsModal
    launchModal: LaunchSessionModal
    empty: ReactNode
    infoLinkFollow: CancelableEventHandler<LinkProps.FollowDetail>
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionWithPermissions>) => DataAccessServiceResult<SessionWithPermissions>
}

export default function SessionsTable({getViewForm,
                                setView,
                                deleteItemsKey,
                                sessionsState,
                                setSessionsState,
                                describeSessionsQuery,
                                getFilter,
                                createButton,
                                actions,
                                connectButton,
                                deleteModal,
                                launchModal,
                                empty,
                                infoLinkFollow,
                                dataAccessServiceFunction}: SessionsTableProps) {
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(SESSIONS_TABLE_DEFAULT_PREFERENCES)
    const [refreshKey, setRefreshKey] = useState("")
    const [resetPaginationKey, setResetPaginationKey] = useState("")

    useEffect(() => {
        setRefreshKey(Date.now().toString())
    }, [useFlashBarContext().items])

    const header = <ConsoleHeader
                                  headerDescription={SESSIONS_CONSTANTS.HEADER_DESCRIPTION}
                                  headerTitle={SESSIONS_CONSTANTS.SESSIONS}
                                  infoLinkFollow={infoLinkFollow}
                                  infoLinkLabel={GLOBAL_CONSTANTS.INFO_LABEL}
                                  actions={<SpaceBetween direction="horizontal" size="xs">
                                      {actions(sessionsState.selectedSession)}
                                      {connectButton(sessionsState.selectedSession)}
                                      {createButton}
                                  </SpaceBetween>}
                                  flashItems={[]}/>

    const table = <Table
        variant={"full-page"}
        columnDefinitions={SESSIONS_TABLE_COLUMN_DEFINITIONS}
        columnDisplay={preferences.contentDisplay}
        selectedItems={sessionsState.selectedSession ? [sessionsState.selectedSession] : []}
        onSelectionChange={event => {
            setSessionsState(prevState => {
                return {
                    ...prevState,
                    selectedSession: event.detail.selectedItems[0]
                }
            })
        }}
        trackBy={SESSIONS_CONSTANTS.ID}
        loadingText={SESSIONS_CONSTANTS.LOADING_TEXT}
        selectionType="single"
        sortingDisabled={true}  // Delete this line it to reenable sorting (once sorting has been fixed post-pagination)
        filter={getFilter(() => setResetPaginationKey(Date.now().toString()))}
        header={header}
        empty={empty}
        resizableColumns={true}
        items={[]}
    />

    return (
        <>
            <TableWithPagination
                table={table}
                header={header}
                deleteItemsKey={deleteItemsKey}
                defaultSortingColumn={SESSIONS_TABLE_COLUMN_DEFINITIONS[0]}
                query={describeSessionsQuery}
                dataAccessServiceFunction={dataAccessServiceFunction}
                preferences={preferences}
                setPreferences={setPreferences}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                resetPaginationKey={resetPaginationKey}
                customPreference={(radioValue, setRadioValue) => getViewForm(radioValue, setRadioValue)}
                setView={setView}
                contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
            />
            {deleteModal}
            {launchModal}
        </>
    )
}