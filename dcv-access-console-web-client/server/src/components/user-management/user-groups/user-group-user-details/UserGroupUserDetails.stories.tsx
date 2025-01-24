import UserGroupUserDetails, {
    UserGroupUserDetailsProps
} from "@/components/user-management/user-groups/user-group-user-details/UserGroupUserDetails";
import * as React from "react";
import {getDescribeUsers200Response} from "@/generated-src/msw/mock";

export default {
    title: 'components/user-management/user-groups/UserGroupUserDetails',
    component: UserGroupUserDetails,
}

const Template = (args: UserGroupUserDetailsProps) => <UserGroupUserDetails{...args}/>

export const UserGroupUserDetailsEmpty = Template.bind({})
UserGroupUserDetailsEmpty.args = {
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
    setRefreshKey: () => {},
}

export const UserGroupUserDetailsLoading= Template.bind({})
UserGroupUserDetailsLoading.args = {
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
    setRefreshKey: () => {},
}

export const UserGroupUserDetailsNormal = Template.bind({})
UserGroupUserDetailsNormal.args = {
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: getDescribeUsers200Response().Users,
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    setRefreshKey: () => {},
}