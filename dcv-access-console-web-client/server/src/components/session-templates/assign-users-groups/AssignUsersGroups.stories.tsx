import AssignUsersGroups, {
    AssignUsersGroupsProps
} from "@/components/session-templates/assign-users-groups/AssignUsersGroups";
import {EDIT_USER_GROUP_FORM_CONSTANTS} from "@/constants/edit-user-group-form-constants";

export default {
    title: 'components/session-templates/AssignUsersGroups',
    component: AssignUsersGroups,
}

const Template = (args: AssignUsersGroupsProps) => <AssignUsersGroups{...args}/>

export const AssignUsersGroupsEmpty = Template.bind({})
AssignUsersGroupsEmpty.args = {
    formConstants: EDIT_USER_GROUP_FORM_CONSTANTS
}
