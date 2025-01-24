import EditGroupTextField
    , {
    EditUserGroupDisplayNameProps
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-display-name/EditGroupTextField";

export default {
    title: 'components/user-management/user-groups/modify-user-group/EditUserGroupDisplayName',
    component: EditGroupTextField,
}

const Template = (args: EditUserGroupDisplayNameProps) => <EditGroupTextField{...args}/>

export const EditUserGroupDisplayNameEmpty = Template.bind({})
EditUserGroupDisplayNameEmpty.args = {
    text: "",
    disabled: false,
    placeholderText: "Placeholder Text",
    description: "Description",
    label: "Label"
}

export const EditUserGroupDisplayNameFilled = Template.bind({})
EditUserGroupDisplayNameFilled.args = {
    text: "Dummy Name",
    disabled: false,
    description: "Description",
    label: "Label"
}

export const EditUserGroupDisplayNameFilledAndDisabled = Template.bind({})
EditUserGroupDisplayNameFilledAndDisabled.args = {
    text: "Entry Text",
    disabled: true,
    description: "Description",
    label: "Label"
}

export const EditUserGroupDisplayNameValidationError = Template.bind({})
EditUserGroupDisplayNameValidationError.args = {
    text: "Entry Text",
    disabled: false,
    description: "Description",
    label: "Label",
    errorText: "Error Text"
}