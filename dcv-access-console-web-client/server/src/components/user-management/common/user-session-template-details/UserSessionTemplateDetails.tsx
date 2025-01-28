// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {CollectionPreferencesProps, Container, Header, SpaceBetween, Table} from "@cloudscape-design/components";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS} from "@/constants/user-group-session-template-details-constants";
import {
    USER_SESSION_TEMPLATE_DETAILS_TABLE_COLUMN_DEFINITIONS
} from "@/components/user-management/common/user-session-template-details/UserSessionTemplateDetailsTableColumnDefinitions";
import {SessionTemplatesState} from "@/components/user-management/common/SplitPanelStates";
import {SessionTemplate} from "@/generated-src/client";
import Button from "@cloudscape-design/components/button";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {
    DataAccessServiceParams,
    DataAccessServiceResult,
    useSessionTemplatesService
} from "@/components/common/hooks/DataAccessServiceHooks";
import {useState} from "react";
import {
    CONTENT_DISPLAY_OPTIONS,
    SESSION_TEMPLATES_DEFAULT_PREFERENCES
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnPreferences";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export type UserSessionTemplateDetailsProps = {
    unpublishSessionTemplate: (sessionTemplate: SessionTemplate) => void,
    addSessionTemplate?: () => void,
    selectedSessionTemplates: SessionTemplate[],
    setSelectedSessionTemplates: (sessionTemplates: SessionTemplate[]) => void,
    query: PropertyFilterQuery,
    additionalQueryParams: { [key: string]: string },
    refreshKey: string,
    setRefreshKey: (key: string) => void,
    resetPaginationKey: string,
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => DataAccessServiceResult<SessionTemplate>,
}

export default function UserSessionTemplateDetails(
    {
        unpublishSessionTemplate,
        addSessionTemplate,
        selectedSessionTemplates,
        setSelectedSessionTemplates,
        query,
        additionalQueryParams,
        refreshKey,
        setRefreshKey,
        resetPaginationKey,
        dataAccessServiceFunction
    }: UserSessionTemplateDetailsProps
) {
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(SESSION_TEMPLATES_DEFAULT_PREFERENCES)
    const [deleteItemsKey, setDeleteItemsKey] = useState("")

    const header = <HeaderWithCounter
        variant={"h3"}
        actions={
            <SpaceBetween direction="horizontal" size="xs">
                <Button variant="normal" disabled={selectedSessionTemplates.length !== 1} onClick={() => {
                    unpublishSessionTemplate(selectedSessionTemplates[0])
                    setSelectedSessionTemplates([])
                    setRefreshKey(Date.now().toString())
                    setDeleteItemsKey(Date.now().toString())
                }}>
                    {USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.REMOVE_SESSION_TEMPLATE_BUTTON}
                </Button>
                {addSessionTemplate ?
                <Button variant="normal" onClick={addSessionTemplate}>
                    {USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.ADD_SESSION_TEMPLATE_BUTTON}
                </Button> : <></>}
            </SpaceBetween>
        }
    >
        {USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATES_HEADER}
    </HeaderWithCounter>

    const table = <Table
        variant="embedded"
        empty={
            <Box textAlign="center" color="inherit">
                <b>{USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.EMPTY_TEXT}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        }
        items={[]}
        loadingText={USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.LOADING_TEXT}
        columnDefinitions={USER_SESSION_TEMPLATE_DETAILS_TABLE_COLUMN_DEFINITIONS}
        selectionType="single"
        onSelectionChange={({ detail }) =>
            setSelectedSessionTemplates(detail.selectedItems)
        }
        selectedItems={selectedSessionTemplates}
        sortingDisabled
    />

    return <>
        <Container>
            <TableWithPagination
                table={table}
                header={header}
                deleteItemsKey={deleteItemsKey}
                defaultSortingColumn={USER_SESSION_TEMPLATE_DETAILS_TABLE_COLUMN_DEFINITIONS[0]}
                query={query}
                dataAccessServiceFunction={dataAccessServiceFunction}
                preferences={preferences}
                setPreferences={setPreferences}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                resetPaginationKey={resetPaginationKey}
                contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
                extraRequestsParams={additionalQueryParams}
            />
        </Container>
    </>
}
