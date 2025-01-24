import EditUserGroup, {EditUserGroupProps} from "@/components/user-management/user-groups/modify-user-group/edit-user-group/EditUserGroup";
import {getDescribeUserGroups200Response} from "@/generated-src/msw/mock";
import CreateUserGroup
    from "@/components/user-management/user-groups/modify-user-group/create-user-group/CreateUserGroup";

export default {
    title: 'components/user-management/user-groups/modify-user-group/CreateUserGroup',
    component: CreateUserGroup
}

const Template = (args: EditUserGroupProps) => <CreateUserGroup {...args} />

export const CreateUserGroupNormal = Template.bind({})
CreateUserGroupNormal.args = {

}