import DcvServerDetails from "@/components/servers/dcv-server-details/DcvServerDetails";
import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";
import {Server} from "@/generated-src/client";

export default {
    title: 'components/server/DcvServerDetails',
    component: DcvServerDetails,
}


const Template = args => <DcvServerDetails{...args}/>
export const DcvServerDetailsEmpty = Template.bind({})
DcvServerDetailsEmpty.args = {
}

export const DcvServerDetailsNormal = Template.bind({})
const serverAvailable: Server = {
    ...SERVERS[0],
    Id: Buffer.from(SERVERS[0].Id).toString('base64'),
    Availability: "AVAILABLE",
    UnavailabilityReason: undefined
}
DcvServerDetailsNormal.args = {
    server: serverAvailable
}

export const DcvServerDetailsServerFull = Template.bind({})
const serverFull: Server = {
    ...SERVERS[0],
    Id: Buffer.from(SERVERS[0].Id).toString('base64'),
    Availability: "UNAVAILABLE",
    UnavailabilityReason: "SERVER_FULL"
}
DcvServerDetailsServerFull.args = {
    server: serverFull
}

export const DcvServerDetailsServerUnavailable = Template.bind({})
const serverUnavailable: Server = {
    ...SERVERS[0],
    Id: Buffer.from(SERVERS[0].Id).toString('base64'),
    Availability: "UNAVAILABLE",
    UnavailabilityReason: "SERVER_CLOSED"
}
DcvServerDetailsServerUnavailable.args = {
    server: serverUnavailable
}
