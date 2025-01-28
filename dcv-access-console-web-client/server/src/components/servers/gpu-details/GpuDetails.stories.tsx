// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";
import GpuDetails from "@/components/servers/gpu-details/GpuDetails";

export default {
    title: 'components/server/GpuDetails',
    component: GpuDetails,
}

const Template = args => <GpuDetails{...args}/>
export const GpuDetailsEmpty = Template.bind({})
GpuDetailsEmpty.args = {
}

export const GpuDetailsNormal = Template.bind({})
GpuDetailsNormal.args = {
    gpus: SERVERS[0].Host.Gpus
}
