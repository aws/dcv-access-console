import {CollectionPreferencesProps} from "@cloudscape-design/components";
import {
    USERS_TABLE_COLUMN_DEFINITIONS
} from "@/components/user-management/users/users-table/UsersTableColumnDefinitions";
import {USERS_TABLE_CONSTANTS} from "@/constants/users-table-constants";

const DEFAULT_DISPLAY_COLUMNS = [USERS_TABLE_CONSTANTS.DISPLAY_NAME_ID, USERS_TABLE_CONSTANTS.USER_ID, USERS_TABLE_CONSTANTS.ROLE_ID, USERS_TABLE_CONSTANTS.LAST_TIME_ACTIVE_ID]
const REMOVE_USER_GROUP_USERS_DISPLAY_COLUMNS = [USERS_TABLE_CONSTANTS.DISPLAY_NAME_ID, USERS_TABLE_CONSTANTS.USER_ID]
export const CONTENT_DISPLAY_OPTIONS: ReadonlyArray<CollectionPreferencesProps.ContentDisplayOption> = USERS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        label: column.header
    }
})

const DEFAULT_CONTENT_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = USERS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: DEFAULT_DISPLAY_COLUMNS.includes(column.id!)
    }
})

const REMOVE_USER_GROUP_USERS_CONTENT_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = USERS_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: REMOVE_USER_GROUP_USERS_DISPLAY_COLUMNS.includes(column.id!)
    }
})

export const DEFAULT_PREFERENCES: CollectionPreferencesProps.Preferences = {
    contentDisplay: DEFAULT_CONTENT_DISPLAY,
    pageSize: 20,
}

export const REMOVE_USER_GROUP_USERS_PREFERENCES: CollectionPreferencesProps.Preferences = {
    contentDisplay: REMOVE_USER_GROUP_USERS_CONTENT_DISPLAY,
    pageSize: 5,
}