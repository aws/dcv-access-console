// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS} from "@/constants/user-group-session-template-details-constants";
import {TableProps} from "@cloudscape-design/components";
import {SessionTemplate} from "@/generated-src/client";
import OsLabel from "@/components/session-templates/os-label/OsLabel";

export const USER_SESSION_TEMPLATE_DETAILS_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<SessionTemplate>[] = [
    {
        id: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_NAME_ID,
        header: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_NAME_HEADER,
        cell: sessionTemplate => sessionTemplate.Name,
        sortingField: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_NAME_ID,
    },
    {
        id: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_OPERATING_SYSTEM_ID,
        header: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_OPERATING_SYSTEM_HEADER,
        cell: sessionTemplate => <OsLabel osFamily={sessionTemplate.OsFamily}/>,
        sortingField: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_OPERATING_SYSTEM_ID,
    },
    {
        id: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_DESCRIPTION_ID,
        header: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_DESCRIPTION_HEADER,
        cell: sessionTemplate => sessionTemplate.Description,
        sortingField: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_DESCRIPTION_ID,
    },
    {
        id: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_SESSION_TYPES_ID,
        header: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_SESSION_TYPES_HEADER,
        cell: sessionTemplate => sessionTemplate.Type,
        sortingField: USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS.SESSION_TEMPLATE_SESSION_TYPES_ID,
    }
]