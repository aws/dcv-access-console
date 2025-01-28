// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {ColumnLayout, Container, Header, SpaceBetween} from "@cloudscape-design/components";
import {CpuInfo, CpuLoadAverage} from "@/generated-src/client";
import {CPU_DETAILS_CONSTANTS} from "@/constants/cpu-details-constants";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import {formatPercentage} from "@/components/common/utils/TextUtils";

export default function CpuDetails({cpu, cpuLoad}: { cpu: CpuInfo | undefined, cpuLoad: CpuLoadAverage | undefined }) {
    if (!cpu || !cpuLoad) {
        return <Box textAlign="center">{CPU_DETAILS_CONSTANTS.EMPTY_TEXT}</Box>;
    }

    return <Container
        header={
            <Header
                variant="h3"
            >
                {CPU_DETAILS_CONSTANTS.CPU_HEADER}
            </Header>
        }
    >
        <ColumnLayout columns={2} variant="text-grid">
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_VENDOR}>{getValueOrUnknown(cpu.Vendor)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_MODEL_NAME}>{getValueOrUnknown(cpu.ModelName)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_NUMBER_OF_CPUS}>{getValueOrUnknown(cpu.NumberOfCpus)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_PHYSICAL_CORE_PER_CPU}>{getValueOrUnknown(cpu.PhysicalCoresPerCpu)}</ValueWithLabel>
            </SpaceBetween>
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_ARCHITECTURE}>{getValueOrUnknown(cpu.Architecture)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_LOAD_AVERAGE_ONE_MINUTE}>{formatPercentage(cpuLoad.OneMinute, 2)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_LOAD_AVERAGE_FIVE_MINUTE}>{formatPercentage(cpuLoad.FiveMinutes, 2)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_LOAD_AVERAGE_FIFTEEN_MINUTE}>{formatPercentage(cpuLoad.FifteenMinutes, 2)}</ValueWithLabel>
            </SpaceBetween>
        </ColumnLayout>
    </Container>
}
