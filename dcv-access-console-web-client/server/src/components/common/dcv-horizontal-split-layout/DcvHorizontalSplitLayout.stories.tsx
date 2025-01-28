// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import DcvHorizontalSplitLayout, {
    DcvHorizontalSplitLayoutProps
} from "@/components/common/dcv-horizontal-split-layout/DcvHorizontalSplitLayout";

export default {
    title: 'components/common/DcvHorizontalSplitLayout',
    component: DcvHorizontalSplitLayout
}

const Template = (args: DcvHorizontalSplitLayoutProps) => <DcvHorizontalSplitLayout{...args}/>
export const DcvHorizontalSplitLayoutWithoutUsernamePassword = Template.bind({})
DcvHorizontalSplitLayoutWithoutUsernamePassword.args = {
    addUsernamePassword: false
}

export const DcvHorizontalSplitLayoutWithUsernamePassword = Template.bind({})
DcvHorizontalSplitLayoutWithUsernamePassword.args = {
    addUsernamePassword: true
}




