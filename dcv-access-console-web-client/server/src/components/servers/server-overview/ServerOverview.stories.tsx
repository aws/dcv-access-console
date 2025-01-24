import ServerOverview, {ServerOverviewProps} from "@/components/servers/server-overview/ServerOverview";
import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";

export default {
    title: 'components/server/ServerOverview',
    component: ServerOverview,
}

const Template = (args: ServerOverviewProps) => <ServerOverview{...args}/>
export const ServerOverviewEmpty = Template.bind({})
ServerOverviewEmpty.args = {
    server: undefined
}

export const ServerOverviewWithServer = Template.bind({})
ServerOverviewWithServer.args = {
    server: SERVERS[0]
}

const serverWithUnknownCpu = {
    ...SERVERS[0],
    Host: {
        CpuLoadAverage: {
            FiveMinutes: undefined
        }
    }

}
export const ServerOverviewWithUnknownCpu = Template.bind({})
ServerOverviewWithUnknownCpu.args = {
    server: serverWithUnknownCpu
}

