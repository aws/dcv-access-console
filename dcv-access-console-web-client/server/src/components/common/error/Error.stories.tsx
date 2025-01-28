// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import Error from "@/components/common/error/Error";

export default {
    title: 'components/common/Error',
    component: Error,
}


const Template = (args) => {
    return <Error{...args}/>
}

export const ErrorNormal = Template.bind({})
ErrorNormal.args = {
}
