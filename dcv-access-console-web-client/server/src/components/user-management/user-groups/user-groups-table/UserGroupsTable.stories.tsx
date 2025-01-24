import UserGroupsTable, {
    UserGroupsTableProps
} from "@/components/user-management/user-groups/user-groups-table/UserGroupsTable";
import {DataAccessServiceParams} from "@/components/common/hooks/DataAccessServiceHooks";
import {UserGroup} from "@/generated-src/client";
import {
    DEFAULT_PREFERENCES as USER_GROUPS_PREFERENCES
} from "@/components/user-management/user-groups/user-groups-table/UserGroupsTableColumnPreferences";

export default {
    title: 'components/user-management/user-groups/UserGroupsTable',
    component: UserGroupsTable
}

const Template = (args: UserGroupsTableProps) => <UserGroupsTable {...args} />;

export const UserGroupsTableEmpty = Template.bind({})
UserGroupsTableEmpty.args = {
    preferences: USER_GROUPS_PREFERENCES,
    query: {
        tokens: [],
        operation: 'and'
    },
    setRefreshKey: () => {},
    dataAccessServiceFunction: (params: DataAccessServiceParams<UserGroup>) => {
        return {
            loading: false,
            items: [],
            nextToken: null,
            totalCount: 0,
            currentPageIndex: 1
        }
    }
}

export const UserGroupsTableNormal = Template.bind({})
UserGroupsTableNormal.args = {
    preferences: USER_GROUPS_PREFERENCES,
    query: {
        tokens: [],
        operation: 'and'
    },
    dataAccessServiceFunction: (params: DataAccessServiceParams<UserGroup>) => {
        return {
            loading: false,
            items: [
                {
                    UserGroupId: "user1",
                    DisplayName: "User 1",
                },
                {
                    UserGroupId: "user2",
                    DisplayName: "User 2",
                }
            ],
            nextToken: null,
            totalCount: 2,
            currentPageIndex: 1
        }
    },
    setRefreshKey: () => {}
}