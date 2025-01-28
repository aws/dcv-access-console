// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {TableProps} from "@cloudscape-design/components";
import {Server} from "@/generated-src/client";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import ServerAvailability from "@/components/servers/server-availability/ServerAvailability";
import {formatFileSize, formatPercentage, formatUserOverTotal} from "@/components/common/utils/TextUtils";
import OsLabel from "@/components/servers/os-label/OsLabel";

export const SEVERS_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<Server>[] =
    [
        {
            id: SERVERS_TABLE_CONSTANTS.OS_ID,
            header: SERVERS_TABLE_CONSTANTS.OS_HEADER,
            cell: server => <OsLabel os={server.Host?.Os}/>,
            sortingField: SERVERS_TABLE_CONSTANTS.OS_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.HOSTNAME_ID,
            header: SERVERS_TABLE_CONSTANTS.HOSTNAME_HEADER,
            cell: server => server.Hostname,
            sortingField: SERVERS_TABLE_CONSTANTS.HOSTNAME_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.MEMORY_TOTAL_BYTES_ID,
            header: SERVERS_TABLE_CONSTANTS.MEMORY_HEADER,
            cell: server => formatUserOverTotal(formatFileSize(server.Host?.Memory?.UsedBytes), formatFileSize(server.Host?.Memory?.TotalBytes)),
            sortingField: SERVERS_TABLE_CONSTANTS.SWAP_TOTAL_BYTES,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.CPU_INFO_ID,
            header: SERVERS_TABLE_CONSTANTS.CPU_HEADER,
            cell: server => formatPercentage(server.Host?.CpuLoadAverage?.FiveMinutes),
            sortingField: SERVERS_TABLE_CONSTANTS.MEMORY_TOTAL_BYTES,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.AVAILABILITY_ID,
            header: SERVERS_TABLE_CONSTANTS.AVAILABILITY_HEADER,
            cell: server => <ServerAvailability availability={server.Availability} unavailabilityReason={server.UnavailabilityReason}/>,
            sortingField: SERVERS_TABLE_CONSTANTS.AVAILABILITY_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.AWS_INSTANCE_TYPE_ID,
            header: SERVERS_TABLE_CONSTANTS.AWS_INSTANCE_TYPE_HEADER,
            cell: server => server.Host?.Aws?.EC2InstanceType,
            sortingField: SERVERS_TABLE_CONSTANTS.AWS_INSTANCE_TYPE_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.DEFAULT_DNS_NAME_ID,
            header: SERVERS_TABLE_CONSTANTS.DEFAULT_DNS_NAME_HEADER,
            cell: server => server.DefaultDnsName,
            sortingField: SERVERS_TABLE_CONSTANTS.DEFAULT_DNS_NAME_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.PORT_ID,
            header: SERVERS_TABLE_CONSTANTS.PORT_HEADER,
            cell: server => server.Port,
            sortingField: SERVERS_TABLE_CONSTANTS.PORT_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.WEB_URL_PATH_ID,
            header: SERVERS_TABLE_CONSTANTS.WEB_URL_PATH_HEADER,
            cell: server => server.WebUrlPath,
            sortingField: SERVERS_TABLE_CONSTANTS.WEB_URL_PATH_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.VERSION_ID,
            header: SERVERS_TABLE_CONSTANTS.VERSION_HEADER,
            cell: server => server.Version,
            sortingField: SERVERS_TABLE_CONSTANTS.VERSION_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.SESSION_MANAGER_AGENT_VERSION_ID,
            header: SERVERS_TABLE_CONSTANTS.SESSION_MANAGER_AGENT_VERSION_HEADER,
            cell: server => server.SessionManagerAgentVersion,
            sortingField: SERVERS_TABLE_CONSTANTS.SESSION_MANAGER_AGENT_VERSION_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.CONSOLE_SESSION_COUNT_ID,
            header: SERVERS_TABLE_CONSTANTS.CONSOLE_SESSION_COUNT_HEADER,
            cell: server => server.ConsoleSessionCount,
            sortingField: SERVERS_TABLE_CONSTANTS.CONSOLE_SESSION_COUNT_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.VIRTUAL_SESSION_COUNT_ID,
            header: SERVERS_TABLE_CONSTANTS.VIRTUAL_SESSION_COUNT_HEADER,
            cell: server => server.VirtualSessionCount,
            sortingField: SERVERS_TABLE_CONSTANTS.VIRTUAL_SESSION_COUNT_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.CPU_LOAD_AVERAGE_ID,
            header: SERVERS_TABLE_CONSTANTS.CPU_LOAD_AVERAGE_HEADER,
            cell: server => server.Host?.CpuLoadAverage?.FifteenMinutes,
            sortingField: SERVERS_TABLE_CONSTANTS.CPU_LOAD_AVERAGE_ID,
        },
        {
            id: SERVERS_TABLE_CONSTANTS.LOGGED_IN_USERS_ID,
            header: SERVERS_TABLE_CONSTANTS.LOGGED_IN_USERS_HEADER,
            cell: server => server.Host?.LoggedInUsers?.length,
            sortingField: SERVERS_TABLE_CONSTANTS.LOGGED_IN_USERS_ID,
        },
    ];
