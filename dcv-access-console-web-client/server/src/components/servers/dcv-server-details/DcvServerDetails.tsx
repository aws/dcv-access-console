// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Server} from "@/generated-src/client";
import {ColumnLayout, Container, Header, SpaceBetween} from "@cloudscape-design/components";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import ServerAvailability from "@/components/servers/server-availability/ServerAvailability";

export default function DcvServerDetails({server}: { server: Server | undefined }) {
    if (!server) {
        return <Box textAlign="center">{SERVER_DETAILS_CONSTANTS.EMPTY_TEXT}</Box>;
    }

    return <Container
            header={
                <Header
                    variant="h3"
                >
                    {SERVER_DETAILS_CONSTANTS.SEVER_DETAILS}
                </Header>
            }
        >
            <ColumnLayout columns={2} variant="text-grid">
                <SpaceBetween size="l">
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.DCV_ID}>{Buffer.from(getValueOrUnknown(server?.Id), 'base64').toString()}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.AVAILABILITY}><ServerAvailability availability={server.Availability} unavailabilityReason={server.UnavailabilityReason}/></ValueWithLabel>
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.VERSION}>{getValueOrUnknown(server?.Version)}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size="l">
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.SESSION_MANAGER_AGENT_VERSION}>{getValueOrUnknown(server?.SessionManagerAgentVersion)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.CONSOLE_SESSION_COUNT}>{getValueOrUnknown(server?.ConsoleSessionCount?.toString())}</ValueWithLabel>
                    <ValueWithLabel
                        label={SERVER_DETAILS_CONSTANTS.VIRTUAL_SESSION_COUNT}>{getValueOrUnknown(server?.VirtualSessionCount?.toString())}</ValueWithLabel>
                </SpaceBetween>
            </ColumnLayout>
        </Container>
}
