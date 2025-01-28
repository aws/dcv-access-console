// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Server} from "@/generated-src/client";
import {SplitPanel} from "@cloudscape-design/components";
import * as React from "react";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import Box from "@cloudscape-design/components/box";
import {ServerDetailTabs} from "@/components/servers/server-detail-tabs/ServerDetailTabs";

export default function ServerSplitPanel({server}: { server: Server | undefined }) {
    if (!server) {
        return <SplitPanel header={SERVER_DETAILS_CONSTANTS.EMPTY_TEXT}
                           i18nStrings={{
                               preferencesConfirm: "Ok",
                               preferencesCancel: "Cancel"
                           }}
        >
            <Box textAlign="center" color="inherit">
                <b>{SERVER_DETAILS_CONSTANTS.EMPTY_TEXT}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        </SplitPanel>
    }
    return <SplitPanel header={server?.Hostname!}
                       i18nStrings={{
                           preferencesConfirm: "Ok",
                           preferencesCancel: "Cancel"
                       }}>
        <ServerDetailTabs server={server}/>
    </SplitPanel>
}
