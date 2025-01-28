// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {USERS_AND_GROUPS_TAB_CONSTANTS} from "@/constants/users-and-groups-tab-constants";
import {TableProps} from "@cloudscape-design/components";
import {User} from "@/generated-src/client";

export const USERS_TAB_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<User>[] = [
    {
        id: USERS_AND_GROUPS_TAB_CONSTANTS.USERNAME,
        header: USERS_AND_GROUPS_TAB_CONSTANTS.USERNAME_HEADER,
        cell: user => user.DisplayName,
        isRowHeader: true
    },{
        id: USERS_AND_GROUPS_TAB_CONSTANTS.USER_ID,
        header: USERS_AND_GROUPS_TAB_CONSTANTS.USER_ID_HEADER,
        cell: user => user.UserId,
        isRowHeader: true
    },
]