import UserGroupDetails, {
    UserGroupDetailsProps
} from "@/components/user-management/user-groups/user-group-details/UserGroupDetails";

export default {
    title: 'components/user-management/user-groups/UserGroupDetails',
    component: UserGroupDetails
}

const Template = (args: UserGroupDetailsProps) => <UserGroupDetails{...args}/>

export const UserGroupDetailsError = Template.bind({})
UserGroupDetailsError.args = {
    error: true
}
export const UserGroupDetailsLoading = Template.bind({})
UserGroupDetailsLoading.args = {
    loading: true
}
export const UserGroupDetailsEmpty = Template.bind({})
UserGroupDetailsEmpty.args = {
}

export const UserGroupDetailsNormal = Template.bind({})
UserGroupDetailsNormal.args = {
    group: {
        UserGroupId: "Group1",
        DisplayName: "User Group 1",
        UserIds: [{}, {}],
        LastModifiedTime: Date.now(),
        CreationTime: Date.now(),
    }
}