import {USERS_AND_GROUPS_TAB_CONSTANTS} from "@/constants/users-and-groups-tab-constants";
import {TableProps} from "@cloudscape-design/components";
import {User, UserGroup} from "@/generated-src/client";

export const GROUPS_TAB_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<UserGroup>[] = [
    {
        id: USERS_AND_GROUPS_TAB_CONSTANTS.GROUP_NAME,
        header: USERS_AND_GROUPS_TAB_CONSTANTS.GROUP_NAME_HEADER,
        cell: group => group.DisplayName,
        isRowHeader: true
    },
]