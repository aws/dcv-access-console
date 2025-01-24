import UserSessionDetails, {
    UserSessionDetailsProps
} from "@/components/user-management/common/user-session-details/UserSessionDetails";
import {getDescribeSessions200Response} from "@/generated-src/msw/mock";
import {DataAccessServiceParams} from "@/components/common/hooks/DataAccessServiceHooks";
import {SessionTemplate} from "@/generated-src/client";

export default {
    title: 'components/users/common/UserSessionDetails',
    component: UserSessionDetails,
}

const Template = (args: UserSessionDetailsProps) => <UserSessionDetails{...args}/>

export const UserSessionDetailsEmpty = Template.bind({})
UserSessionDetailsEmpty.args = {
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: [],
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    selectedItems: [],
    setRefreshKey: () => {}
}

export const UserSessionDetailsLoading = Template.bind({})
UserSessionDetailsLoading.args = {
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: () => {
        return {
            loading: true,
            items: [],
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    selectedItems: [],
    setRefreshKey: () => {}
}

export const UserSessionDetailsNormal = Template.bind({})
const sessions = getDescribeSessions200Response().Sessions;
UserSessionDetailsNormal.args = {
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: sessions,
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    selectedItems: [],
    setRefreshKey: () => {}
}

export const UserSessionDetailsOneSelected = Template.bind({})
UserSessionDetailsOneSelected.args = {
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: sessions,
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    setRefreshKey: () => {},
    selectedItems: [sessions[0]]
}