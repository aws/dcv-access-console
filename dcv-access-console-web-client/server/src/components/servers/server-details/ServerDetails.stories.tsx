import ServerDetails, {ServerDetailsProps} from "@/components/servers/server-details/ServerDetails";
import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";

export default {
    title: 'components/server/ServerDetails',
    component: ServerDetails,
}

const Template = (args: ServerDetailsProps) => <ServerDetails{...args}/>
export const ServerDetailsEmpty = Template.bind({})
ServerDetailsEmpty.args = {
    userInfo: {
        displayName: "Test User",
        email: "test.user@email.does.not.exist",
    }
}

export const ServerDetailsWithServer = Template.bind({})
ServerDetailsWithServer.args = {
    userInfo: {
        displayName: "Test User",
        email: "test.user@email.does.not.exist",
    },
    server: SERVERS[0]
}

export const ServerDetailsLoading = Template.bind({})
ServerDetailsLoading.args = {
    userInfo: {
        displayName: "Test User",
        email: "test.user@email.does.not.exist",
    },
    server: SERVERS[0],
    loading: true
}

export const ServerDetailsError = Template.bind({})
ServerDetailsError.args = {
    userInfo: {
        displayName: "Test User",
        email: "test.user@email.does.not.exist",
    },
    server: SERVERS[0],
    error: true
}
