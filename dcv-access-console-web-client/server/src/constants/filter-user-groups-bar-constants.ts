export const SEARCH_TOKEN_TO_ID: Map<string, string> = new Map<string, string>([
    ["UserGroupIds", "UserGroupId"],
    ["DisplayNames", "DisplayName"],
    ["IsImported", "IsImported"],
    ["CreationTimes", "CreationTime"],
    ["LastModifiedTimes", "LastModifiedTime"]
])

export const TOKEN_NAMES_MAP: Record<string, string> = {
    "UserGroupIds": "Group Id",
    "DisplayNames": "Group name",
    "IsImported": "Imported",
    "CreationTimes": "Creation time",
    "LastModifiedTimes": "Last modified time"
}

export const TOKEN_NAME_GROUP_MAP: Record<string, string> = {
    "UserGroupIds": "Group Id values",
    "DisplayNames": "Group name values",
    "IsImported": "Imported values",
    "CreationTimes": "Creation time values",
    "LastModifiedTimes": "Last modified time values"
}

export const FILTER_USER_GROUPS_CONSTANTS = {
    filteringPlaceholder: "Search user groups"
}