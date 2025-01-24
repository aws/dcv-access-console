import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";
import HostDetails from "@/components/servers/host-details/HostDetails";

export default {
    title: 'components/server/HostDetails',
    component: HostDetails,
}


const Template = args => <HostDetails{...args}/>
export const HostDetailsEmpty = Template.bind({})
HostDetailsEmpty.args = {
}

export const HostDetailsNormal = Template.bind({})
HostDetailsNormal.args = {
    server: SERVERS[0]
}

export const HostDetailsWithFormattedSize = Template.bind({})

const formattedServer = {
    ...SERVERS[0],
    Host: {
        ...SERVERS[0].Host,
        LoggedInUsers: undefined,
        Memory: {
            TotalBytes: undefined,
            UsedBytes: 0,
        },
        Swap: {
            TotalBytes: 2147483648,
            UsedBytes: 2048,
        }
    }
}

HostDetailsWithFormattedSize.args = {
    server: formattedServer
}
