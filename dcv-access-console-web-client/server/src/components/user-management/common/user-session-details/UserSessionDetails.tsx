// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {CollectionPreferencesProps, Container, SpaceBetween, Table} from "@cloudscape-design/components";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {useState} from "react";
import {USER_GROUP_SESSION_DETAILS_CONSTANTS} from "@/constants/user-group-session-details-constants";
import {
    USER_SESSION_DETAILS_TABLE_COLUMN_DEFINITIONS
} from "@/components/user-management/common/user-session-details/UserSessionDetailsTableColumnDefinitions";
import Button from "@cloudscape-design/components/button";
import {Session, SessionWithPermissions} from "@/generated-src/client";
import DeleteSessionsModal, {DeleteSessionProps} from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import LaunchSessionModal from "@/components/sessions/launch-session-modal/LaunchSessionModal";
import {getNativeOsName} from "@/components/common/utils/ClientUtils";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {DataAccessServiceParams, DataAccessServiceResult} from "@/components/common/hooks/DataAccessServiceHooks";
import {
    CONTENT_DISPLAY_OPTIONS,
    DEFAULT_PREFERENCES
} from "@/components/user-management/common/user-session-details/UserSesssionDetailsTableColumnPreferences";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export type UserSessionDetailsProps = {
    deleteSession: (props: DeleteSessionProps) => void,
    selectedItems: SessionWithPermissions[],
    setSelectedItems: (items: SessionWithPermissions[]) => void,
    query: PropertyFilterQuery,
    additionalQueryParams: { [key: string]: string },
    refreshKey: string,
    setRefreshKey: (key: string) => void,
    resetPaginationKey: string,
    dataAccessServiceFunction: (params: DataAccessServiceParams<Session>) => DataAccessServiceResult<Session>,

}

export default function UserSessionDetails(
    {
        deleteSession,
        selectedItems,
        setSelectedItems,
        query,
        additionalQueryParams,
        refreshKey,
        setRefreshKey,
        resetPaginationKey,
        dataAccessServiceFunction
    }: UserSessionDetailsProps
) {
    const [deleteSessionModalVisible, setDeleteSessionModalVisible] = useState<boolean>(false);
    const [launchSessionModalVisible, setLaunchSessionModalVisible] = useState<boolean>(false);
    const [deleteItemsKey, setDeleteItemsKey] = useState("")
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(DEFAULT_PREFERENCES)

    const dataAccessService = new DataAccessService()

    const header = <HeaderWithCounter
        variant={"h3"}
        actions={
            <SpaceBetween direction="horizontal" size="xs">
                <Button
                    variant="normal"
                    disabled={
                        !selectedItems || selectedItems?.length === 0 || selectedItems[0]?.State === "DELETING" || selectedItems[0].State === "CREATING"
                    }
                    onClick={() => {
                        setDeleteSessionModalVisible(true)
                        setDeleteItemsKey(Date.now().toString())
                }}>{USER_GROUP_SESSION_DETAILS_CONSTANTS.CLOSE_SESSION_BUTTON}
                </Button>
                <Button variant="normal" disabled={
                        !selectedItems || selectedItems?.length === 0 || selectedItems[0]?.State === "DELETING" || selectedItems[0].State === "CREATING"
                    }
                    onClick={() => {
                    //Launching the web client
                    dataAccessService.launchSession("web", selectedItems[0].Id)
                }}>{USER_GROUP_SESSION_DETAILS_CONSTANTS.CONNECT_SESSION_BUTTON}
                </Button>
            </SpaceBetween>
        }
    >
        {USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSIONS_HEADER}
    </HeaderWithCounter>

    const sessionsTable = <Table
        variant="embedded"
        items={[]}
        empty={
            <Box textAlign="center" color="inherit">
                <b>{USER_GROUP_SESSION_DETAILS_CONSTANTS.EMPTY_TEXT}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        }
        loadingText={USER_GROUP_SESSION_DETAILS_CONSTANTS.LOADING_TEXT}
        columnDefinitions={USER_SESSION_DETAILS_TABLE_COLUMN_DEFINITIONS}
        selectionType="single"
        onSelectionChange={({ detail }) => {
            setSelectedItems(detail.selectedItems)
        }}
        selectedItems={selectedItems}
    />

    return <>
            <Container>
                <TableWithPagination
                    table={sessionsTable}
                    header={header}
                    deleteItemsKey={deleteItemsKey}
                    defaultSortingColumn={USER_SESSION_DETAILS_TABLE_COLUMN_DEFINITIONS[0]}
                    query={query}
                    preferences={preferences}
                    setPreferences={setPreferences}
                    dataAccessServiceFunction={dataAccessServiceFunction}
                    extraRequestsParams={additionalQueryParams}
                    refreshKey={refreshKey}
                    setRefreshKey={setRefreshKey}
                    resetPaginationKey={resetPaginationKey}
                    contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
                />
            </Container>
            <DeleteSessionsModal
                visible={deleteSessionModalVisible}
                setVisible={setDeleteSessionModalVisible}
                deleteSessionProps={{
                    sessionId: selectedItems[0]?.Id,
                    owner: selectedItems[0]?.Owner,
                    sessionName: selectedItems[0]?.Name,
                }}
                closeSession={deleteSession}
            />
            <LaunchSessionModal
                visible={launchSessionModalVisible}
                setVisible={setLaunchSessionModalVisible}
                launchingSessionProps={{
                    sessionName: selectedItems[0]?.Name,
                    clientName: getNativeOsName()
                }}
            />
    </>
}