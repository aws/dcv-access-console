// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import DeleteUserGroupModal from "@/components/user-management/user-groups/delete-user-group-modal/DeleteUserGroupModal";

export default {
    title: 'components/user-management/user-groups/DeleteUserGroupModal',
    component: DeleteUserGroupModal,
}

const Template = args => <DeleteUserGroupModal{...args}/>

export const DeleteUserGroupModalNormal = Template.bind({})
DeleteUserGroupModalNormal.args = {
    visible: true,
    setVisible: (visible: boolean) => {},
    deleteUserGroup: (userGroupId: string, userGroupDisplayName: string) => {},
    deleteUserGroupProps: {
        userGroupId: "test-id",
        userGroupDisplayName: "display name",
    }
}