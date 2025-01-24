import ServerEndpoints from "@/components/servers/server-endpoints/ServerEndpoints";
import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";

export default {
    title: 'components/server/ServerEndpoints',
    component: ServerEndpoints,
}
const Template = (args) => <ServerEndpoints {...args} />
export const EmptyServerEndpoints = Template.bind({})
EmptyServerEndpoints.args = {}

export const ServerEndpointsNormal = Template.bind({})
ServerEndpointsNormal.args = {
    endpoints: SERVERS[0].Endpoints
}
