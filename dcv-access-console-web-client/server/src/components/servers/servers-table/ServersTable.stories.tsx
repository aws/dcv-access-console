import ServersTable, {ServersTableProps} from "@/components/servers/servers-table/ServersTable";
import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";
import * as React from "react";
import {
    ALL_CONTENT_DISPLAY,
    DEFAULT_PREFERENCES
} from "@/components/servers/servers-table/ServersTableColumnPreferences";
import {DataAccessServiceParams} from "@/components/common/hooks/DataAccessServiceHooks";
import {SessionTemplate, UserGroup} from "@/generated-src/client";

export default {
    title: 'components/server/ServersTable',
    component: ServersTable,
}

const Template = (args: ServersTableProps) => <ServersTable{...args}/>

export const ServerTableEmpty = Template.bind({})
ServerTableEmpty.args = {
    onSelectionChange: () => {},
    loading: false,
    alerts: [],
    preferences: DEFAULT_PREFERENCES,
    setPreferences: () => {},
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            items: [],
            loading: false,
            nextToken: null
        }
    }
}

export const ServerTableNormal = Template.bind({})
ServerTableNormal.args = {
    selectedServer: SERVERS[0],
    onSelectionChange: () => {},
    alerts: [],
    preferences: DEFAULT_PREFERENCES,
    setPreferences: () => {},
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            items: SERVERS,
            totalCount: SERVERS.length,
            loading: false,
            nextToken: null,
            currentPageIndex: 1,
        }
    },
}

export const ServerTableAllColumns = Template.bind({})
ServerTableAllColumns.args = {
    onSelectionChange: () => {},
    alerts: [],
    preferences: {contentDisplay: ALL_CONTENT_DISPLAY},
    setPreferences: () => {},
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            items: SERVERS,
            totalCount: SERVERS.length,
            loading: false,
            nextToken: null,
            currentPageIndex: 1,
        }
    }
}
