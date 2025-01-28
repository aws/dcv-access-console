// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Host, Server} from "@/generated-src/client";
import {ColumnLayout, Container, Header, SpaceBetween} from "@cloudscape-design/components";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {HOST_DETAILS_CONSTANTS} from "@/constants/host-details-constants";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {formatFileSize, usersToCommaSeperatedString} from "@/components/common/utils/TextUtils";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import OsLabel from "@/components/servers/os-label/OsLabel";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";

export default function HostDetails({server}: { server: Server | undefined }) {
    if (!server) {
        return <Box textAlign="center">{HOST_DETAILS_CONSTANTS.EMPTY_TEXT}</Box>;
    }
    const host = server?.Host
    return <Container
            header={
                <Header
                    variant="h3"
                >
                    {HOST_DETAILS_CONSTANTS.HOST_DETAILS}
                </Header>
            }
        >
            <ColumnLayout columns={3} variant="text-grid">
                <SpaceBetween size="l">
                    <Box variant="h4" padding={{top: "s"}}>{SERVERS_TABLE_CONSTANTS.HOST_HEADER}</Box>
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.DCV_NAME}>{getValueOrUnknown(server.Hostname)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.DCV_IP}>{getValueOrUnknown(server?.Ip)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.LOGGED_IN_USERS_HEADER}>{usersToCommaSeperatedString(host?.LoggedInUsers)}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size="l">
                    <Box variant="h4" padding={{top: "s"}}>{SERVERS_TABLE_CONSTANTS.OS_HEADER}</Box>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.OS_FAMILY}> <OsLabel os={host?.Os}/> </ValueWithLabel>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.OS_NAME}>{getValueOrUnknown(host?.Os?.Name)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.OS_VERSION}>{getValueOrUnknown(host?.Os?.Version)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.OS_KERNEL_VERSION}>{getValueOrUnknown(host?.Os?.KernelVersion)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.OS_BUILD_NUMBER}>{getValueOrUnknown(host?.Os?.BuildNumber)}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size={"l"}>
                    <Box variant="h4" padding={{top: "s"}}>{SERVERS_TABLE_CONSTANTS.MEMORY_HEADER}</Box>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.MEMORY_TOTAL_BYTES}>{formatFileSize(host?.Memory?.TotalBytes)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.MEMORY_USED_BYTES}>{formatFileSize(host?.Memory?.UsedBytes)}</ValueWithLabel>
                    <Box variant="h4" padding={{top: "s"}}>{SERVERS_TABLE_CONSTANTS.SWAP_HEADER}</Box>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.SWAP_TOTAL_BYTES}>{formatFileSize(host?.Swap?.TotalBytes)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVERS_TABLE_CONSTANTS.SWAP_USED_BYTES}>{formatFileSize(host?.Swap?.UsedBytes)}</ValueWithLabel>
                </SpaceBetween>
            </ColumnLayout>
        </Container>
}
