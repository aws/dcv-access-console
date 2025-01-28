// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {FILTER_CONSTANTS} from "@/constants/generic-search-bar-constants";

export const SEARCH_TOKEN_TO_ID: Map<string, string> = new Map<string, string>([
    ["CreatedBy", "CreatedBy"],
    ["CreationTimes", "CreationTime"],
    ["Names", "Name"],
    ["Descriptions", "Description"],
    ["OsFamilies", "OsFamily"],
    ["OsVersions", "OsVersions"],
    ["InstanceIds", "InstanceIds"],
    ["InstanceTypes", "InstanceTypes"],
    ["InstanceRegions", "InstanceRegions"],
    ["HostNumberOfCpus", "HostNumberOfCpus"],
    ["HostMemoryTotalBytes", "HostMemoryTotalBytes"],
    ["LastModifiedTimes", "LastModifiedTime"],
    ["LastModifiedBy", "LastModifiedBy"],
    ["Types", "Type"],
    ["AutorunFiles", "AutorunFile"],
    ["MaxConcurrentClients", "MaxConcurrentClients"],
    ["InitFiles", "InitFile"],
    ["StorageRoots", "StorageRoot"],
    ["PermissionsFiles", "PermissionsFile"],
    ["Requirements", "Requirements"],
    ["AutorunFileArguments", "AutorunFileArguments"],
    ["DcvGlEnabled", "DcvGlEnabled"]
])

export const TOKEN_NAMES_MAP: Record<string, string> = {
    "CreatedBy": "Created By",
    "CreationTimes": "Creation time",
    "Names": "Name",
    "Descriptions": "Description",
    "OsFamilies": "OS family",
    "OsVersions": "OS versions",
    "InstanceIds": "Instance IDs",
    "InstanceTypes": "Instance Types",
    "InstanceRegions": "Instance Regions",
    "HostNumberOfCpus": "Number of CPUs on Host",
    "HostMemoryTotalBytes": "Total Bytes of Memory on Host",
    "LastModifiedTimes": "Last modified time",
    "LastModifiedBy": "Last modified by",
    "Types": "Type",
    "AutorunFiles": "Autorun file",
    "MaxConcurrentClients": "Max concurrent clients",
    "InitFiles": "Init file",
    "StorageRoots": "Storage root",
    "PermissionsFiles": "Permissions file",
    "Requirements": "Requirements",
    "AutorunFileArguments": "Autorun file arguments",
    "DcvGlEnabled": "DcvGl enabled",
    "UsersSharedWith": "Users shared with",
    "GroupsSharedWith": "Groups shared with"
}

export const TOKEN_NAME_GROUP_MAP: Record<string, string> = {
    "Ids": "Template ID Values",
    "CreationTimes": "Creation Time Values",
    "CreatedBy": "Created By Values",
    "Names": "Name Values",
    "Descriptions": "Description Values",
    "OsFamilies": "OS Family Values",
    "OsVersions":  "OS Version Values",
    "InstanceIds": "Instance ID Values",
    "InstanceTypes": "Instance Type Values",
    "InstanceRegions": "Instance Region Values",
    "HostNumberOfCpus": "Number of CPUs on Host Values",
    "HostMemoryTotalBytes": "Total Bytes of Memory on Host Values",
    "LastModifiedTimes": "Last Modified Time Values",
    "LastModifiedBy": "Last Modified By Values",
    "Types": "Type Values",
    "AutorunFiles": "Autorun File Values",
    "MaxConcurrentClients": "Max Concurrent Clients Values",
    "InitFiles": "Init File Values",
    "StorageRoots": "Storage Root Values",
    "PermissionsFiles": "Permissions File Values",
    "Requirements": "Requirements Values",
    "AutorunFileArguments": "Autorun File Arguments Values",
    "DcvGlEnabled": "DcvGl Enabled Values",
    "UsersSharedWith": FILTER_CONSTANTS.filteringEmpty,
    "GroupsSharedWith": FILTER_CONSTANTS.filteringEmpty
}

export const FILTER_SESSION_TEMPLATES_CONSTANTS = {
    filteringPlaceholder: "Search session templates"
}
