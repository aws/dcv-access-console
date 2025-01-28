// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import UserGroupsSplitPanel, {
    UserGroupsSplitPanelProps
} from "@/components/user-management/user-groups/user-groups-split-panel/UserGroupsSplitPanel";
import {AppLayout} from "@cloudscape-design/components";

export default {
    title: 'components/user-management/user-groups/UserGroupsSplitPanel',
    component: UserGroupsSplitPanel,
}

const Template = (args: UserGroupsSplitPanelProps) => <AppLayout splitPanel={<UserGroupsSplitPanel{...args}/>}/>

export const UserGroupsSplitPanelEmpty = Template.bind({})
UserGroupsSplitPanelEmpty.args = {
    group: undefined
}

export const UserGroupsSplitPanelWithGroup = Template.bind({})
UserGroupsSplitPanelWithGroup.args = {
    group: {
        UserGroupId: "user1",
        DisplayName: "User 1",
    }
}