// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Server} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {SERVER_DETAIL_TABS_CONSTANTS} from "@/constants/server-detail-tabs-constants";
import {Tabs} from "@cloudscape-design/components";
import {TabsProps} from "@cloudscape-design/components/tabs/interfaces";
import {SERVER_SPLIT_PANEL_DETAILS_CONSTANTS} from "@/constants/server-split-panel-constants";
import DcvServerDetails from "@/components/servers/dcv-server-details/DcvServerDetails";
import ServerEndpoints from "@/components/servers/server-endpoints/ServerEndpoints";
import HostDetails from "@/components/servers/host-details/HostDetails";
import CpuDetails from "@/components/servers/cpu-details/CpuDetails";
import GpuDetails from "@/components/servers/gpu-details/GpuDetails";
import ServerTags from "@/components/sessions/ServerTags";
import AwsDetails from "@/components/servers/aws-details/AwsDetails";

export type ServerDetailTabsProps = {
    server: Server | undefined
}
export function getTabs(server: Server): TabsProps.Tab[] {
    let tabs: TabsProps.Tab[] = [
        {
            label: SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.HOST,
            id: "host",
            content: <HostDetails server={server}/>
        },
        {
            label: SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.DCV_SERVER,
            id: "dcv_server",
            content: <DcvServerDetails server={server}/>
        },
        {
            label: SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.CPU,
            id: "cpu",
            content: <CpuDetails cpu={server.Host?.CpuInfo} cpuLoad={server.Host?.CpuLoadAverage}/>
        },
        {
            label: SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.GPU,
            id: "gpu",
            content: <GpuDetails gpus={server.Host?.Gpus}/>
        },
        {
            label: SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.SERVER_ENDPOINTS,
            id: "endpoints",
            content: <ServerEndpoints endpoints={server.Endpoints || []}/>
        },
        {
            label: SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.TAGS,
            id: "tags",
            content: <ServerTags tags={server?.Tags!}/>,
        }
    ]
    if (server.Host?.Aws) {
        tabs.splice(1, 0, {
            label: SERVER_SPLIT_PANEL_DETAILS_CONSTANTS.AWS,
            id: "aws",
            content: <AwsDetails aws={server.Host?.Aws}/>
        })
    }
    return tabs
}

export function ServerDetailTabs(props: ServerDetailTabsProps) {
    if (!props.server) {
        return <Box textAlign="center" color="inherit">
            <b>{SERVER_DETAIL_TABS_CONSTANTS.EMPTY_TEXT}</b>
            <Box
                padding={{bottom: "s"}}
                variant="p"
                color="inherit"
            >
            </Box>
        </Box>
    }
    return <Tabs
        tabs={getTabs(props.server)}
    />
}
