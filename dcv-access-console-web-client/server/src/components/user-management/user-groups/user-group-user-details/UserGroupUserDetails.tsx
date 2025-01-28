// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Container, Table} from "@cloudscape-design/components";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {USER_GROUP_USER_DETAILS_CONSTANTS} from "@/constants/user-group-user-details-constants";
import {
    USER_GROUP_USER_DETAILS_TABLE_COLUMN_DEFINITIONS
} from "@/components/user-management/user-groups/user-group-user-details/UserGroupUserDetailsTableColumnDefinitions";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {
    DataAccessServiceParams,
    DataAccessServiceResult
} from "@/components/common/hooks/DataAccessServiceHooks";
import {useState} from "react";
import {
    CONTENT_DISPLAY_OPTIONS,
    DEFAULT_PREFERENCES
} from "@/components/user-management/users/users-table/UsersTableColumnPreferences";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";
import {User} from "@/generated-src/client";

export type UserGroupUserDetailsProps = {
    query: PropertyFilterQuery,
    refreshKey: string,
    setRefreshKey: (key: string) => void,
    resetPaginationKey: string,
    dataAccessServiceFunction: (params: DataAccessServiceParams<User>) => DataAccessServiceResult<User>,

}

export default function UserGroupUserDetails(
    {
        query,
        refreshKey,
        setRefreshKey,
        resetPaginationKey,
        dataAccessServiceFunction
    }: UserGroupUserDetailsProps) {

    const [preferences, setPreferences]= useState(DEFAULT_PREFERENCES)

    const header = <HeaderWithCounter variant={"h3"}>
        {USER_GROUP_USER_DETAILS_CONSTANTS.USERS_HEADER}
    </HeaderWithCounter>

    const table = <Table
        variant="embedded"
        empty={
            <Box textAlign="center" color="inherit">
                <b>{USER_GROUP_USER_DETAILS_CONSTANTS.EMPTY_TEXT}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        }
        loadingText={USER_GROUP_USER_DETAILS_CONSTANTS.LOADING_TEXT}
        items={[]}
        columnDefinitions={USER_GROUP_USER_DETAILS_TABLE_COLUMN_DEFINITIONS}
        sortingDisabled
    />

    return <Container>
        <TableWithPagination
            table={table}
            header={header}
            defaultSortingColumn={USER_GROUP_USER_DETAILS_TABLE_COLUMN_DEFINITIONS[0]}
            query={query}
            dataAccessServiceFunction={dataAccessServiceFunction}
            preferences={preferences}
            setPreferences={setPreferences}
            refreshKey={refreshKey}
            setRefreshKey={setRefreshKey}
            resetPaginationKey={resetPaginationKey}
            contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
        />
    </Container>
}