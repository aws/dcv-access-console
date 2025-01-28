// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import OsLabel from "@/components/session-templates/os-label/OsLabel";
import {OsFamily} from "@/generated-src/client";

export default {
    title: 'components/session-templates/OsLabel',
    component: OsLabel,
}
const Template = args => <OsLabel{...args}/>

export const OsLabelLinux = Template.bind({})
OsLabelLinux.args = {
    osFamily: OsFamily.Linux
}

export const OsLabelWindows = Template.bind({})
OsLabelWindows.args = {
    osFamily: OsFamily.Windows
}

export const OsLabelUnknown = Template.bind({})
OsLabelUnknown.args = {
}
