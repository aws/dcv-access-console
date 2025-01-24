import EditUserGroupForm, {
    EditUserGroupFormProps
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-form/EditUserGroupForm";
import {EDIT_USER_GROUP_FORM_CONSTANTS} from "@/constants/edit-user-group-form-constants";
import {CREATE_USER_GROUP_FORM_CONSTANTS} from "@/constants/create-user-group-form-constants";

export default {
    title: 'components/user-management/user-groups/modify-user-group/EditUserGroupForm',
    component: EditUserGroupForm,
}

const Template = (args: EditUserGroupFormProps) => <EditUserGroupForm{...args}/>

export const CreateUserGroupFormEmpty = Template.bind({})
CreateUserGroupFormEmpty.args = {
    group: {

    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,
    isEditPage: true
}

export const CreateUserGroupFormPopulated = Template.bind({})
CreateUserGroupFormPopulated.args = {
    group: {
        UserGroupId: "Test ID",
        DisplayName: "Test Display Name",
    },
    formConstants: CREATE_USER_GROUP_FORM_CONSTANTS,
    isEditPage: true
}
export const EditUserGroupFormPopulated = Template.bind({})
EditUserGroupFormPopulated.args = {
    group: {
        UserGroupId: "Test ID",
        DisplayName: "Test Display Name",
    },
    formConstants: EDIT_USER_GROUP_FORM_CONSTANTS,
    isEditPage: true
}

