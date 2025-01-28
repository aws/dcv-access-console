// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import EditUserGroupSessionTemplates, {
    EditUserGroupSessionTemplatesProps
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-session-templates/EditUserGroupSessionTemplates";
import {getDescribeSessionTemplates200Response} from "@/generated-src/msw/mock";
import {DescribeSessionTemplatesRequestData} from "@/generated-src/client";
import {CREATE_USER_GROUP_FORM_CONSTANTS} from "@/constants/create-user-group-form-constants";

export default {
    title: 'components/user-management/user-groups/modify-user-group/EditUserGroupSessionTemplates',
    component: EditUserGroupSessionTemplates
}

const Template = (args: EditUserGroupSessionTemplatesProps) => <EditUserGroupSessionTemplates {...args}/>


export const EditUserGroupSessionTemplatesLoading = Template.bind({})
EditUserGroupSessionTemplatesLoading.args = {
    selectSessionTemplatesState: {
        status: "loading",
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,
}

export const EditUserGroupSessionTemplatesError = Template.bind({})
EditUserGroupSessionTemplatesError.args = {
    selectSessionTemplatesState: {
        status: "error",
        errorMessage: "Test Message"
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,

}
export const EditUserGroupSessionTemplatesEmpty = Template.bind({})
EditUserGroupSessionTemplatesEmpty.args = {
    selectSessionTemplatesState: {
        status: "finished",
        sessionTemplates: []
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,
}

const sessionTemplates = getDescribeSessionTemplates200Response().SessionTemplates.map(sessionTemplate => {
    return {
        value: sessionTemplate.Id,
        label: sessionTemplate.Name,
        description: sessionTemplate.Description
    }
})
export const EditUserGroupSessionTemplatesPopulatedNoneSelected = Template.bind({})
EditUserGroupSessionTemplatesPopulatedNoneSelected.args = {
    selectSessionTemplatesState: {
        status: "finished",
        options: sessionTemplates
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,

}
export const EditUserGroupSessionTemplatesPopulatedSomeSelected = Template.bind({})
EditUserGroupSessionTemplatesPopulatedSomeSelected.args = {
    sessionTemplatesSharedWithGroup: sessionTemplates.slice(0, 2),
    selectSessionTemplatesState: {
        status: "finished",
        options: sessionTemplates
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,

}