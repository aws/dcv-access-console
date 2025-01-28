// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import AddUserGroupUsers
    from "@/components/user-management/user-groups/modify-user-group/add-user-group-users/AddUserGroupUsers";
import {getDescribeUsers200Response} from "@/generated-src/msw/mock";
import {CREATE_USER_GROUP_FORM_CONSTANTS} from "@/constants/create-user-group-form-constants";
import RemoveUserGroupUsers, {
    RemoveUserGroupUsersProps
} from "@/components/user-management/user-groups/modify-user-group/remove-user-group-users/RemoveUserGroupUsers";

export default {
    title: 'components/user-management/user-groups/modify-user-group/RemoveUserGroupUsers',
    component: AddUserGroupUsers
}

const Template = (args: RemoveUserGroupUsersProps) => <RemoveUserGroupUsers {...args} />


export const RemoveUserGroupUsersLoading = Template.bind({})
RemoveUserGroupUsersLoading.args = {
    dataAccessServiceFunction: () => {
        return {
            loading: true,
            items: [],
        }
    },
    query: {
        tokens: [],
        operation: "and"
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,
}

export const RemoveUserGroupUsersEmpty = Template.bind({})
RemoveUserGroupUsersEmpty.args = {
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: [],
        }
    },
    query: {
        tokens: [],
        operation: "and"
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,
}

export const RemoveUserGroupUsersPopulated = Template.bind({})
RemoveUserGroupUsersPopulated.args = {
    dataAccessServiceFunction: () => {
        return {
            loading: false,
            items: getDescribeUsers200Response().Users,
        }
    },
    query: {
        tokens: [],
        operation: "and"
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,
}