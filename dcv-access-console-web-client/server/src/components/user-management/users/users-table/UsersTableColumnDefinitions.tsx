import {User} from "@/generated-src/client";
import {USERS_TABLE_CONSTANTS} from "@/constants/users-table-constants";
import {TableProps} from "@cloudscape-design/components";
import {formatDate} from "@/components/common/utils/TextUtils";

export const USERS_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<User>[] =
    [
        {
            id: USERS_TABLE_CONSTANTS.DISPLAY_NAME_ID,
            header: USERS_TABLE_CONSTANTS.DISPLAY_NAME_HEADER,
            cell: user => user.DisplayName,
            sortingField: USERS_TABLE_CONSTANTS.DISPLAY_NAME_ID,
        },
        {
            id: USERS_TABLE_CONSTANTS.USER_ID,
            header: USERS_TABLE_CONSTANTS.USER_HEADER,
            cell: user => user.UserId,
            sortingField: USERS_TABLE_CONSTANTS.USER_ID,
        },
        {
            id: USERS_TABLE_CONSTANTS.ROLE_ID,
            header: USERS_TABLE_CONSTANTS.ROLE_HEADER,
            cell: user => user.Role,
            sortingField: USERS_TABLE_CONSTANTS.ROLE_ID,
        },
        {
            id: USERS_TABLE_CONSTANTS.LAST_TIME_ACTIVE_ID,
            header: USERS_TABLE_CONSTANTS.LAST_TIME_ACTIVE_HEADER,
            cell: user => formatDate(user.LastLoggedInTime),
            sortingField: USERS_TABLE_CONSTANTS.LAST_TIME_ACTIVE_ID,
        },
        {
            id: USERS_TABLE_CONSTANTS.LAST_MODIFIED_TIME_ID,
            header: USERS_TABLE_CONSTANTS.LAST_MODIFIED_TIME_HEADER,
            cell: user => formatDate(user.LastModifiedTime),
            sortingField: USERS_TABLE_CONSTANTS.LAST_MODIFIED_TIME_ID,
        },
        {
            id: USERS_TABLE_CONSTANTS.CREATION_TIME_ID,
            header: USERS_TABLE_CONSTANTS.CREATION_TIME_HEADER,
            cell: user => formatDate(user.CreationTime),
            sortingField: USERS_TABLE_CONSTANTS.CREATION_TIME_ID,
        },
        {
            id: USERS_TABLE_CONSTANTS.IS_IMPORTED_ID,
            header: USERS_TABLE_CONSTANTS.IS_IMPORTED_HEADER,
            cell: user => user.IsImported.toString(),
            sortingField: USERS_TABLE_CONSTANTS.IS_IMPORTED_ID,
        }
    ]