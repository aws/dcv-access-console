import {
    FormField
} from "@cloudscape-design/components";
import * as React from "react";
import {User} from "@/generated-src/client";
import {PropertyFilterQuery, PropertyFilterToken} from "@cloudscape-design/collection-hooks";
import {
    DEFAULT_FILTERING_QUERY,
    useUsersService
} from "@/components/common/hooks/DataAccessServiceHooks";
import {
    REMOVE_USER_GROUP_USERS_PREFERENCES
} from "@/components/user-management/users/users-table/UsersTableColumnPreferences";
import UsersTable from "@/components/user-management/users/users-table/UsersTable";
import {REMOVE_USER_GROUPS_USERS_TOKEN_NAMES_MAP} from "@/constants/filter-users-bar-constants";
import {useState} from "react";
import {PropertyFilterProps} from "@cloudscape-design/components/property-filter";

export type RemoveUserGroupUsersProps = {
    selectedUsers: User[],
    setSelectedUsers: (users: User[]) => void,
    query: PropertyFilterQuery,
    setQuery: (query: PropertyFilterQuery) => void,
    usersInGroupFilterToken: PropertyFilterToken
}
export default function RemoveUserGroupUsers(
    {
        selectedUsers,
        setSelectedUsers,
        query,
        setQuery,
        usersInGroupFilterToken
    }: RemoveUserGroupUsersProps) {
    const [filterBarQuery, setFilterBarQuery] = useState<PropertyFilterProps.Query>(DEFAULT_FILTERING_QUERY)

    return <FormField
        stretch
    >
        <UsersTable
            selectedUsers={selectedUsers}
            setSelectedUsers={setSelectedUsers}
            addHeader={false}
            variant={"borderless"}
            selectionType={"multi"}
            columnPreferences={REMOVE_USER_GROUP_USERS_PREFERENCES}
            query={query}
            setQuery={setQuery}
            usersInGroupFilterToken={usersInGroupFilterToken}
            filterBarQuery={filterBarQuery}
            setFilterBarQuery={setFilterBarQuery}
            filterTokenNamesMap={REMOVE_USER_GROUPS_USERS_TOKEN_NAMES_MAP}
            dataAccessServiceFunction={useUsersService}
        />
    </FormField>
}