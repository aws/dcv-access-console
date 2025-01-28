// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {FILTER_CONSTANTS} from "@/constants/generic-search-bar-constants";

export const SEARCH_TOKEN_TO_ID: Map<string, string> = new Map<string, string>([
    ["UserIds", "UserId"],
    ["DisplayNames", "DisplayName"],
    ["Roles", "Role"],
    ["IsImported", "IsImported"],
    ["CreationTimes", "CreationTime"],
    ["LastModifiedTimes", "LastModifiedTime"],
    ["LastLoggedInTimes", "LastLoggedInTime"]
])

export const TOKEN_NAMES_MAP: Record<string, string> = {
    "UserIds": "User Id",
    "DisplayNames": "User name",
    "Roles": "Role",
    "IsImported": "Imported",
    "CreationTimes": "Creation time",
    "LastModifiedTimes": "Last modified time",
    "LastLoggedInTimes": "Last time active",
    "UserGroupIds": "User group Id"
}

export const REMOVE_USER_GROUPS_USERS_TOKEN_NAMES_MAP: Record<string, string> = {
    "UserIds": "User Id",
    "DisplayNames": "User name",
    "Roles": "Role",
    "IsImported": "Imported",
    "CreationTimes": "Creation time",
    "LastModifiedTimes": "Last modified time",
    "LastLoggedInTimes": "Last time active"
}

export const TOKEN_NAME_GROUP_MAP: Record<string, string> = {
    "UserIds": "User Id values",
    "DisplayNames": "User name values",
    "Roles": "Role values",
    "IsImported": "Imported values",
    "CreationTimes": "Creation time values",
    "LastModifiedTimes": "Last modified time values",
    "LastLoggedInTimes": "Last time active values",
    "UserGroupIds": FILTER_CONSTANTS.filteringEmpty
}

export const FILTER_USERS_CONSTANTS = {
    filteringPlaceholder: "Search users"
}