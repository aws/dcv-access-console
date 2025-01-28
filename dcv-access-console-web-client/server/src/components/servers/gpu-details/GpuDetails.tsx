// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Gpu} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {GPU_DETAILS_CONSTANTS} from "@/constants/gpu-details-constants";
import {SERVER_TAG_CONSTANTS} from "@/constants/server-tags-constants";
import Table from "@cloudscape-design/components/table";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {Container, Header} from "@cloudscape-design/components";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";

export default function GpuDetails({gpus}: { gpus: Gpu[] | undefined }) {
    return <Container>
            <Header
                variant="h3"
            >
                {GPU_DETAILS_CONSTANTS.GPU_HEADER}
            </Header>
            <Table
                variant="embedded"
                columnDefinitions={[
                    {
                        id: GPU_DETAILS_CONSTANTS.GPU_VENDOR_ID,
                        header: SERVERS_TABLE_CONSTANTS.GPU_VENDOR,
                        cell: e => e.Vendor,
                        minWidth: 165,
                        isRowHeader: true
                    },
                    {
                        id: GPU_DETAILS_CONSTANTS.GPU_MODEL_ID,
                        header: GPU_DETAILS_CONSTANTS.GPU_MODEL,
                        cell: e => e.ModelName,
                        minWidth: 165,
                    }
                ]}
                items={gpus!}
                loadingText={SERVER_TAG_CONSTANTS.LOADING_TEXT}
                resizableColumns
                empty={
                    <Box textAlign="center" color="inherit">
                        <b>{SERVER_DETAILS_CONSTANTS.UNKNOWN}</b>
                        <Box
                            padding={{bottom: "s"}}
                            variant="p"
                            color="inherit"
                        >
                        </Box>
                    </Box>
                }
            />
    </Container>
}
