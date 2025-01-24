import {USER_GROUP_SESSION_DETAILS_CONSTANTS} from "@/constants/user-group-session-details-constants";
import {TableProps} from "@cloudscape-design/components";
import {SessionWithPermissions} from "@/generated-src/client";
import StatusLabel from "@/components/sessions/sessions-cards/status-label/StatusLabel";
import * as React from "react";

export const USER_SESSION_DETAILS_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<SessionWithPermissions>[] = [
    {
        id: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_NAME_ID,
        header: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_NAME_HEADER,
        cell: session => session.Name,
        sortingField: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_NAME_ID
    },
    {
        id: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_LEVEL_OF_ACCESS_ID,
        header: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_LEVEL_OF_ACCESS_HEADER,
        cell: session => session.LevelOfAccess,
        sortingField: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_LEVEL_OF_ACCESS_ID
    },
    {
        id: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_STATUS_ID,
        header: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_STATUS_HEADER,
        cell: session => <StatusLabel state={session.State}/>,
        sortingField: USER_GROUP_SESSION_DETAILS_CONSTANTS.SESSION_STATUS_ID
    }
]