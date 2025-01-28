// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import SessionTemplatesTable, {
    SessionTemplatesTableProps
} from "@/components/session-templates/session-templates-table/SessionTemplatesTable";
import {SESSION_TEMPLATES} from "@/components/session-templates/session-templates-table/SessionTemplatesTableMocks";
import * as React from "react";
import {
    SESSION_TEMPLATES_DEFAULT_PREFERENCES
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnPreferences";
import {DataAccessServiceParams} from "@/components/common/hooks/DataAccessServiceHooks";
import {SessionTemplate} from "@/generated-src/client";

export default {
    title: 'components/session-templates/SessionTemplatesTable',
    component: SessionTemplatesTable,
}

const Template = (args: SessionTemplatesTableProps) => <SessionTemplatesTable{...args}/>

export const SessionTemplatesTableEmpty = Template.bind({})
SessionTemplatesTableEmpty.args = {
    onSelectionChange: () => {},
    alerts: [],
    preferences: SESSION_TEMPLATES_DEFAULT_PREFERENCES,
    setPreferences: () => {},
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
    }
}

export const SessionTemplatesTableNormal = Template.bind({})
SessionTemplatesTableNormal.args = {
    selectedSessionTemplate: SESSION_TEMPLATES[0],
    onSelectionChange: () => {},
    alerts: [],
    preferences: SESSION_TEMPLATES_DEFAULT_PREFERENCES,
    setPreferences: () => {},
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => {
        return {
            loading: false,
            items: SESSION_TEMPLATES,
            nextToken: null,
            totalCount: SESSION_TEMPLATES.length,
            currentPageIndex: 1
        }
    }
}
