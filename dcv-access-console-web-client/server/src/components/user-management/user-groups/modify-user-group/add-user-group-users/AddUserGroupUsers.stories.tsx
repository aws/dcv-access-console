// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import AddUserGroupUsers
    , {
    AddUserGroupUsersProps
} from "@/components/user-management/user-groups/modify-user-group/add-user-group-users/AddUserGroupUsers";
import {getDescribeUsers200Response} from "@/generated-src/msw/mock";
import {CREATE_USER_GROUP_FORM_CONSTANTS} from "@/constants/create-user-group-form-constants";

export default {
    title: 'components/user-management/user-groups/modify-user-group/AddUserGroupUsers',
    component: AddUserGroupUsers
}

const Template = (args: AddUserGroupUsersProps) => <AddUserGroupUsers {...args} />


export const AddUserGroupUsersLoading = Template.bind({})
AddUserGroupUsersLoading.args = {
    selectUsersState: {
        status: "loading"
    },
    formConstants: {
        LOADING: CREATE_USER_GROUP_FORM_CONSTANTS.LOADING_USERS_TEXT,
        EMPTY: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_EMPTY_TEXT,
        ERROR: CREATE_USER_GROUP_FORM_CONSTANTS.USER_SEARCH_ERROR_MESSAGE,
        PLACEHOLDER: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_PLACEHOLDER
    }

}

export const AddUserGroupUsersError = Template.bind({})
AddUserGroupUsersError.args = {
    selectUsersState: {
        status: "error",
        errorMessage: "Test Message"
    },
    formConstants: {
        LOADING: CREATE_USER_GROUP_FORM_CONSTANTS.LOADING_USERS_TEXT,
        EMPTY: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_EMPTY_TEXT,
        ERROR: CREATE_USER_GROUP_FORM_CONSTANTS.USER_SEARCH_ERROR_MESSAGE,
        PLACEHOLDER: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_PLACEHOLDER
    }

}
export const AddUserGroupUsersEmpty = Template.bind({})
AddUserGroupUsersEmpty.args = {
    selectUsersState: {
        users: [],
        state: "finished",
    },
    formConstants: {
        LOADING: CREATE_USER_GROUP_FORM_CONSTANTS.LOADING_USERS_TEXT,
        EMPTY: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_EMPTY_TEXT,
        ERROR: CREATE_USER_GROUP_FORM_CONSTANTS.USER_SEARCH_ERROR_MESSAGE,
        PLACEHOLDER: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_PLACEHOLDER
    }

}

export const AddUserGroupUsersPopulated = Template.bind({})
AddUserGroupUsersPopulated.args = {
    selectUsersState: {
        options: getDescribeUsers200Response().Users.map(user => {
            return {
                value: user.UserId,
                label: user.DisplayName
            }
        }),
        state: "finished"
    },
    formConstants: {
        LOADING: CREATE_USER_GROUP_FORM_CONSTANTS.LOADING_USERS_TEXT,
        EMPTY: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_EMPTY_TEXT,
        ERROR: CREATE_USER_GROUP_FORM_CONSTANTS.USER_SEARCH_ERROR_MESSAGE,
        PLACEHOLDER: CREATE_USER_GROUP_FORM_CONSTANTS.ADD_USER_PLACEHOLDER
    }
}
