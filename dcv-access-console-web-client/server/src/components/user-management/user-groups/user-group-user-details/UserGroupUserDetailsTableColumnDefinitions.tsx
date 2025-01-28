// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {USER_GROUP_USER_DETAILS_CONSTANTS} from "@/constants/user-group-user-details-constants";
import {TableProps} from "@cloudscape-design/components";
import {User} from "@/generated-src/client";
import {formatDate} from "@/components/common/utils/TextUtils";

export const USER_GROUP_USER_DETAILS_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<User>[] = [
    {
        id: USER_GROUP_USER_DETAILS_CONSTANTS.USER_DISPLAY_NAME_ID,
        header: USER_GROUP_USER_DETAILS_CONSTANTS.USER_DISPLAY_NAME_HEADER,
        cell: user => user.DisplayName,
        sortingField: USER_GROUP_USER_DETAILS_CONSTANTS.USER_DISPLAY_NAME_ID
    },
    {
        id: USER_GROUP_USER_DETAILS_CONSTANTS.USER_ID,
        header: USER_GROUP_USER_DETAILS_CONSTANTS.USER_ID_HEADER,
        cell: user => user.UserId,
        sortingField: USER_GROUP_USER_DETAILS_CONSTANTS.USER_ID
    },
    {
        id: USER_GROUP_USER_DETAILS_CONSTANTS.LAST_TIME_ACTIVE_ID,
        header: USER_GROUP_USER_DETAILS_CONSTANTS.LAST_TIME_ACTIVE_HEADER,
        cell: user => formatDate(user.LastLoggedInTime),
        sortingField: USER_GROUP_USER_DETAILS_CONSTANTS.LAST_TIME_ACTIVE_ID
    }
]