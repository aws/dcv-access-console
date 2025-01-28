// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {ServerDetailTabs, ServerDetailTabsProps} from "@/components/servers/server-detail-tabs/ServerDetailTabs";
import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";

export default {
    title: 'components/server/ServerDetailTabs',
    component: ServerDetailTabs,
}
const Template = (args: ServerDetailTabsProps) => <ServerDetailTabs{...args}/>
export const ServerDetailTabsEmpty = Template.bind({})
ServerDetailTabsEmpty.args = {
    server: undefined
}

export const ServerDetailTabsWithServerWithAws = Template.bind({})
ServerDetailTabsWithServerWithAws.args = {
    server: SERVERS[0]
}

export const ServerDetailTabsWithoutAWS = Template.bind({})
const serverWithoutAWS = {
    ...SERVERS[0],
    Endpoints: undefined,
    Host: {
        Aws: undefined
    }
}
ServerDetailTabsWithoutAWS.args = {
    server: serverWithoutAWS
}
