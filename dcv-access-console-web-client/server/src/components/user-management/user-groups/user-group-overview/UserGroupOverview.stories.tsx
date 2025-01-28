// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import UserGroupOverview, {
    UserGroupOverviewProps
} from "@/components/user-management/user-groups/user-group-overview/UserGroupOverview";

export default {
    title: 'components/user-management/user-groups/UserGroupOverview',
    component: UserGroupOverview
}

const Template = (args: UserGroupOverviewProps) => <UserGroupOverview{...args}/>

export const UserGroupOverviewEmpty = Template.bind({})
UserGroupOverviewEmpty.args = {

}
export const UserGroupOverviewNormal = Template.bind({})
UserGroupOverviewNormal.args = {
    group: {
        UserGroupId: "Group1",
        DisplayName: "User Group 1",
        UserIds: [{}, {}],
        LastModifiedTime: Date.now(),
        CreationTime: Date.now(),
    }
}