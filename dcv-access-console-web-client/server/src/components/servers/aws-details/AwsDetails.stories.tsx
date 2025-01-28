// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {SERVERS} from "@/components/servers/servers-table/ServersTableMocks";
import AwsDetails from "@/components/servers/aws-details/AwsDetails";

export default {
    title: 'components/server/AwsDetails',
    component: AwsDetails,
}

const Template = args => <AwsDetails{...args}/>
export const AwsDetailsEmpty = Template.bind({})
AwsDetailsEmpty.args = {
}

export const AwsDetailsNormal = Template.bind({})
AwsDetailsNormal.args = {
    aws: SERVERS[0].Host.Aws
}
