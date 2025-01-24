import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";
import ServerSplitPanel from "@/components/servers/server-split-panel/ServerSplitPanel";
import {AppLayout} from "@cloudscape-design/components";

export default {
    title: 'components/server/ServerSplitPanel',
    component: ServerSplitPanel,
}

const Template = args => <AppLayout splitPanel={<ServerSplitPanel{...args}/>}/>

export const ServerSplitPanelEmpty = Template.bind({})
ServerSplitPanelEmpty.args = {
}

export const ServerSplitPanelNormal = Template.bind({})
ServerSplitPanelNormal.args = {
    server: SERVERS[0]
}

export const ServerSplitPanelWithoutAWS = Template.bind({})
const serverWithoutAWS = {
    ...SERVERS[0],
    Endpoints: undefined,
    Host: {
        Aws: undefined
    }
}
ServerSplitPanelWithoutAWS.args = {
    server: serverWithoutAWS
}
