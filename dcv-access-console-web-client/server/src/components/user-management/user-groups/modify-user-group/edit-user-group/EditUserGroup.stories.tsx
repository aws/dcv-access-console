import EditUserGroup, {EditUserGroupProps} from "@/components/user-management/user-groups/modify-user-group/edit-user-group/EditUserGroup";
import {getDescribeUserGroups200Response} from "@/generated-src/msw/mock";

export default {
    title: 'components/user-management/user-groups/modify-user-group/EditUserGroup',
    component: EditUserGroup
}

const Template = (args: EditUserGroupProps) => <EditUserGroup {...args} />

export const EditUserGroupLoading = Template.bind({})
EditUserGroupLoading.args = {
    loading: true
}

export const EditUserGroupError = Template.bind({})
EditUserGroupError.args = {
    loading: false,
    error: true,
}

export const EditUserGroupNormal = Template.bind({})
EditUserGroupNormal.args = {
    loading: false,
    error: false,
    group: {
        UserGroupId: "Test ID",
        DisplayName: "Test Display Name",
    }
}