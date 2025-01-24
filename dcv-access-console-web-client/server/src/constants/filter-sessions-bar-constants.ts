export const SEARCH_TOKEN_TO_ID: Map<string, string> = new Map<string, string>([
    ["SessionIds", "Id"],
    ["SessionNames", "Name"],
    ["Owners", "Owner"],
    ["Types", "Type"],
    ["States", "State"],
    ["Ips", "Server.Ip"],
    ["Hostnames", "Server.Hostname"],
    ["CreationTimes", "CreationTime"],
    ["LastDisconnectionTimes", "LastDisconnectionTime"],
    ["NumOfConnections", "NumOfConnections"],
    ["Tags", "Server.Tags"]
])

export const TOKEN_NAMES_MAP: Record<string, string> = {
    "SessionIds": "Session ID",
    "SessionNames": "Session name",
    "Owners": "Owner",
    "Types": "Type",
    "States": "Status",
    "Ips": "IP",
    "Hostnames": "Hostname",
    "CreationTimes": "Creation time",
    "LastDisconnectionTimes": "Last disconnection time",
    "NumOfConnections": "Number of connections",
    "Tags": "Tags",
    // "UserSharedWith": "User shared with",
    // "GroupSharedWith": "Group shared with"
}

export const TOKEN_NAME_GROUP_MAP: Record<string, string> = {
    "SessionIds": "Session ID values",
    "SessionNames": "Session names values",
    "Owners": "Owner values",
    "Types": "Type values",
    "States": "Status values",
    "Ips": "IP values",
    "Hostnames": "Hostname values",
    "CreationTimes": "Creation time values",
    "LastDisconnectionTimes": "Last disconnection time values",
    "NumOfConnections": "Number of connections values",
    "Tags": "Tags values",
    // "UserSharedWith": "User shared with values",
    // "GroupSharedWith": "Group shared with values"
}

export const FILTER_SESSIONS_CONSTANTS = {
    filteringPlaceholder: "Search sessions"
}