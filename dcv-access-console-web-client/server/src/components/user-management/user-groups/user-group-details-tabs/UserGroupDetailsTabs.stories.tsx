// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {UserGroupDetailsTabs} from "@/components/user-management/user-groups/user-group-details-tabs/UserGroupDetailsTabs";

export default {
    title: 'components/user-management/user-groups/UserGroupDetailsTabs',
    component: UserGroupDetailsTabs
}

const Template = (args) => <UserGroupDetailsTabs {...args}/>

export const UserGroupDetailsTabsEmpty = Template.bind({})
UserGroupDetailsTabsEmpty.args = {
    userGroup: {}
}

export const UserGroupDetailsTabsNormal = Template.bind({})
UserGroupDetailsTabsNormal.args = {
    group: {
        UserGroupId: "TestID",
        DisplayName: 'Test Group',
    }
}
