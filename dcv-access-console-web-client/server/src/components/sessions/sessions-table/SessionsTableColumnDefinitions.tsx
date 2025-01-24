import {TableProps} from "@cloudscape-design/components";
import {SessionWithPermissions} from "@/generated-src/client";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import StatusLabel from "@/components/sessions/sessions-cards/status-label/StatusLabel";
import {capitalizeFirstLetter, formatDate} from "@/components/common/utils/TextUtils";
import OsLabel from "@/components/servers/os-label/OsLabel";

export const SESSIONS_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<SessionWithPermissions>[] =
    [
        {
            id: SESSIONS_CONSTANTS.ID,
            header: SESSIONS_CONSTANTS.ID_HEADER,
            cell: session => session.Id,
            sortingField: SESSIONS_CONSTANTS.ID
        },
        {
            id: SESSIONS_CONSTANTS.NAME_ID,
            header: SESSIONS_CONSTANTS.NAME_HEADER,
            cell: session => session.Name,
            sortingField: SESSIONS_CONSTANTS.NAME_ID
        },
        {
            id: SESSIONS_CONSTANTS.STATUS_ID,
            header: SESSIONS_CONSTANTS.STATUS_HEADER,
            cell: session => <StatusLabel state={session.State}/>,
            sortingField: SESSIONS_CONSTANTS.STATUS_ID
        },
        {
            id: SESSIONS_CONSTANTS.OWNER_ID,
            header: SESSIONS_CONSTANTS.OWNER_HEADER,
            cell: session => session.Owner,
            sortingField: SESSIONS_CONSTANTS.OWNER_ID
        },
        {
            id: SESSIONS_CONSTANTS.OS_ID,
            header: SESSIONS_CONSTANTS.OS_HEADER,
            cell: session => <OsLabel os={session.Server?.Host?.Os}/>,
            sortingField: SESSIONS_CONSTANTS.OS_ID,
        },
        {
            id: SESSIONS_CONSTANTS.IP_ADDRESS_ID,
            header: SESSIONS_CONSTANTS.IP_ADDRESS_HEADER,
            cell: session => session.Server?.Ip,
            sortingField: SESSIONS_CONSTANTS.IP_ADDRESS_ID,
        },
        {
            id: SESSIONS_CONSTANTS.HOSTNAME_ID,
            header: SESSIONS_CONSTANTS.HOSTNAME_HEADER,
            cell: session => session.Server?.Hostname,
            sortingField: SESSIONS_CONSTANTS.HOSTNAME_ID,
        },
        {
            id: SESSIONS_CONSTANTS.CPU_ID,
            header: SESSIONS_CONSTANTS.CPU_HEADER,
            cell: session => session.Server?.Host?.CpuInfo?.ModelName,
            sortingField: SESSIONS_CONSTANTS.CPU_ID,
        },
        {
            id: SESSIONS_CONSTANTS.GPU_ID,
            header: SESSIONS_CONSTANTS.GPU_HEADER,
            cell: session => session.Server?.Host?.Gpus?.map(gpu => gpu.ModelName).join(";"),
            sortingField: SESSIONS_CONSTANTS.GPU_ID,
        },
        {
            id: SESSIONS_CONSTANTS.LAST_TIME_CONNECTED_ID,
            header: SESSIONS_CONSTANTS.LAST_TIME_CONNECTED_HEADER,
            cell: session => formatDate(session.LastDisconnectionTime),
            sortingField: SESSIONS_CONSTANTS.LAST_TIME_CONNECTED_ID,
        },
        {
            id: SESSIONS_CONSTANTS.NUM_OF_CONNECTIONS_ID,
            header: SESSIONS_CONSTANTS.NUM_OF_CONNECTIONS_HEADER,
            cell: session => session.NumOfConnections,
            sortingField: SESSIONS_CONSTANTS.NUM_OF_CONNECTIONS_ID,
        },
        {
            id: SESSIONS_CONSTANTS.CREATION_TIME_ID,
            header: SESSIONS_CONSTANTS.CREATION_TIME_HEADER,
            cell: session => formatDate(session.CreationTime),
            sortingField: SESSIONS_CONSTANTS.CREATION_TIME_ID,
        },
    ]
