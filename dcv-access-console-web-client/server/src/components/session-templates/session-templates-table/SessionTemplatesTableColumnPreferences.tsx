import {CollectionPreferencesProps} from "@cloudscape-design/components";
import {SESSION_TEMPLATES_TABLE_CONSTANTS} from "@/constants/session-templates-table-constants";
import {SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnDefinitions";

const DEFAULT_DISPLAY_COLUMNS = [
    SESSION_TEMPLATES_TABLE_CONSTANTS.NAME_ID,
    SESSION_TEMPLATES_TABLE_CONSTANTS.OS_FAMILY_ID,
    SESSION_TEMPLATES_TABLE_CONSTANTS.DESCRIPTION_ID,
    SESSION_TEMPLATES_TABLE_CONSTANTS.TYPE_ID,
]

const CREATE_SESSION_COLUMNS = [
    SESSION_TEMPLATES_TABLE_CONSTANTS.NAME_ID,
    SESSION_TEMPLATES_TABLE_CONSTANTS.OS_FAMILY_ID,
    SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_TYPES_ID,
    SESSION_TEMPLATES_TABLE_CONSTANTS.DESCRIPTION_ID,
    SESSION_TEMPLATES_TABLE_CONSTANTS.HOST_NUM_OF_CPUS_ID
]

export const CONTENT_DISPLAY_OPTIONS: ReadonlyArray<CollectionPreferencesProps.ContentDisplayOption> = SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        label: column.header
    }
})

const DEFAULT_CONTENT_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: DEFAULT_DISPLAY_COLUMNS.includes(column.id!)
    }
})

const CREATE_SESSION_DISPLAY: ReadonlyArray<CollectionPreferencesProps.ContentDisplayItem> = SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS.map(column => {
    return {
        id: column.id!,
        visible: CREATE_SESSION_COLUMNS.includes(column.id!)
    }
})

export const SESSION_TEMPLATES_DEFAULT_PREFERENCES: CollectionPreferencesProps.Preferences = {
    contentDisplay: DEFAULT_CONTENT_DISPLAY,
    pageSize: 20,
}

export const CREATE_SESSION_PREFERENCES: CollectionPreferencesProps.Preferences = {
    contentDisplay: CREATE_SESSION_DISPLAY,
    pageSize: 20,
}

