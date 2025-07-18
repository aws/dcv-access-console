// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";

export default {
    title: 'components/common/TopNavBar',
    component: TopNavBar
}

const Template = args => <TopNavBar{...args}/>
export const TopNavNormal = Template.bind({})
TopNavNormal.args = {
    userInfo: {
        displayName: "Test User",
        email: "test.user@email.does.not.exist",
    }
}
