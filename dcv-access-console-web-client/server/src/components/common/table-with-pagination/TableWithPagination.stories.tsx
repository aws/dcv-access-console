import TableWithPagination, {
    TableWithPaginationProps
} from "@/components/common/table-with-pagination/TableWithPagination";
import {SessionTemplate} from "@/generated-src/client";
import {DataAccessServiceParams} from "@/components/common/hooks/DataAccessServiceHooks";
import {
    SESSION_TEMPLATES_DEFAULT_PREFERENCES
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnPreferences";
import * as React from "react";
import {SESSION_TEMPLATES} from "@/components/session-templates/session-templates-table/SessionTemplatesTableMocks";
import {Table} from "@cloudscape-design/components";
import {
    SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnDefinitions";

export default {
    title: 'components/common/TableWithPagination',
    component: TableWithPagination<SessionTemplate>
}

const table = <Table
        items={[]}
        columnDefinitions={SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS}
        empty={<div>Test Empty</div>}
    />

const Template = (args: TableWithPaginationProps<SessionTemplate>) => <TableWithPagination<SessionTemplate>{...args}/>

export const TableWithPaginationEmpty = Template.bind({})
TableWithPaginationEmpty.args = {
    preferences: SESSION_TEMPLATES_DEFAULT_PREFERENCES,
    setPreferences: () => {},
    table: table,
    header: undefined,
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            loading: false,
            items: [],
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    refreshKey: "",
    setRefreshKey: () => {},
    resetPaginationKey: ""
}

export const TableWithPaginationLoading = Template.bind({})
TableWithPaginationLoading.args = {
    preferences: SESSION_TEMPLATES_DEFAULT_PREFERENCES,
    setPreferences: () => {},
    table: table,
    header: undefined,
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            loading: true,
            items: [],
            nextToken: "token",
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    refreshKey: "",
    setRefreshKey: () => {},
    resetPaginationKey: ""
}

export const TableWithPaginationNormalMorePages = Template.bind({})
TableWithPaginationNormalMorePages.args = {
    preferences: SESSION_TEMPLATES_DEFAULT_PREFERENCES,
    setPreferences: () => {},
    table: table,
    header: undefined,
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            loading: false,
            items: SESSION_TEMPLATES,
            nextToken: "token",
            totalCount: SESSION_TEMPLATES.length,
            currentPageIndex: 1
        }
    },
    refreshKey: "",
    setRefreshKey: () => {},
    resetPaginationKey: ""
}

export const TableWithPaginationNormalNoMorePages = Template.bind({})
TableWithPaginationNormalNoMorePages.args = {
    preferences: SESSION_TEMPLATES_DEFAULT_PREFERENCES,
    setPreferences: () => {},
    table: table,
    header: undefined,
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            loading: false,
            items: SESSION_TEMPLATES.slice(0, 5),
            nextToken: null,
            totalCount: 5,
            currentPageIndex: 1
        }
    },
    refreshKey: "",
    setRefreshKey: () => {},
    resetPaginationKey: ""
}