// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {UserGroup} from "@/generated-src/client";
import {USER_GROUPS_TABLE_CONSTANTS} from "@/constants/user-groups-table-constants";
import {TableProps} from "@cloudscape-design/components";

export const USER_GROUPS_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<UserGroup>[] =
    [
        {
            id: USER_GROUPS_TABLE_CONSTANTS.GROUP_ID,
            header: USER_GROUPS_TABLE_CONSTANTS.GROUP_ID_HEADER,
            cell: group => group.UserGroupId,
            sortingField: USER_GROUPS_TABLE_CONSTANTS.GROUP_ID,
            width: 170
        },
        {
            id: USER_GROUPS_TABLE_CONSTANTS.GROUP_NAME,
            header: USER_GROUPS_TABLE_CONSTANTS.GROUP_NAME_HEADER,
            cell: group => group.DisplayName,
            sortingField: USER_GROUPS_TABLE_CONSTANTS.GROUP_NAME,
        },
        {
            id: USER_GROUPS_TABLE_CONSTANTS.NUMBER_OF_USERS_ID,
            header: USER_GROUPS_TABLE_CONSTANTS.NUMBER_OF_USERS_HEADER,
            cell: group => group.UserIds?.length,
            sortingField: USER_GROUPS_TABLE_CONSTANTS.NUMBER_OF_USERS_ID,
        }
    ]