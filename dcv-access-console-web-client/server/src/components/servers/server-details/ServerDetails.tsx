// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Server} from "@/generated-src/client";
import {ContentLayout, SpaceBetween, StatusIndicator} from "@cloudscape-design/components";
import * as React from "react";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import {ServerDetailTabs} from "@/components/servers/server-detail-tabs/ServerDetailTabs";
import ServerOverview from "@/components/servers/server-overview/ServerOverview";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import Box from "@cloudscape-design/components/box";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export type ServerDetailsProps = {
    server: Server,
    loading: boolean,
    error: boolean,
}
export default function ServerDetails(props: ServerDetailsProps) {
    let content
    if (props.loading) {
        content =  <ContentLayout>
            <SpaceBetween size={"l"} direction={"vertical"}>
                <Box textAlign="center" color="inherit">
                    <StatusIndicator type="loading">
                        {SERVER_DETAILS_CONSTANTS.LOADING_TEXT}
                    </StatusIndicator>
                </Box>
            </SpaceBetween>
        </ContentLayout>
    }
    else if (props.error) {
        content = <ContentLayout>
            <Box textAlign="center" color="inherit">
                <StatusIndicator type="error">
                    {SERVER_DETAILS_CONSTANTS.ERROR_TEXT}
                </StatusIndicator>
            </Box>
        </ContentLayout>
    }
    else {
        content = <ContentLayout
            header={<HeaderWithCounter>{getValueOrUnknown(props.server?.Hostname)}</HeaderWithCounter>}
        >
        <SpaceBetween size={"l"}>
            <ServerOverview server={props.server}/>
            <ServerDetailTabs server={props.server}/>
        </SpaceBetween>
    </ContentLayout>
    }
    return content
}
