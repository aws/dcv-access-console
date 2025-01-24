import UserSessionTemplateDetails, {
    UserSessionTemplateDetailsProps
} from "@/components/user-management/common/user-session-template-details/UserSessionTemplateDetails";
import {getDescribeSessionTemplates200Response} from "@/generated-src/msw/mock";

export default {
    title: 'components/users/common/UserSessionTemplateDetails',
    component: UserSessionTemplateDetails
}

const Template = (args: UserSessionTemplateDetailsProps) => <UserSessionTemplateDetails{...args}/>

export const UserSessionTemplateDetailsEmpty = Template.bind({})
UserSessionTemplateDetailsEmpty.args = {
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
    selectedSessionTemplates: []
}
export const UserSessionTemplateDetailsLoading = Template.bind({})
UserSessionTemplateDetailsLoading.args = {
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
    selectedSessionTemplates: []
}

const sessionTemplates = getDescribeSessionTemplates200Response().SessionTemplates
export const UserSessionTemplateDetailsNormal = Template.bind({})
UserSessionTemplateDetailsNormal.args = {
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: sessionTemplates,
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    setRefreshKey: () => {},
    selectedSessionTemplates: []
}

export const UserSessionTemplateDetailsOneSelected = Template.bind({})
UserSessionTemplateDetailsOneSelected.args = {
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: sessionTemplates,
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    },
    setRefreshKey: () => {},
    selectedSessionTemplates: [sessionTemplates[0]]
}