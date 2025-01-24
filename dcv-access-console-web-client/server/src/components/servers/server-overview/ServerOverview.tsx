import {Server} from "@/generated-src/client";
import {ColumnLayout, Container, Header, SpaceBetween} from "@cloudscape-design/components";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import {SERVER_OVERVIEW_CONSTANTS} from "@/constants/server-overview-contants";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import * as React from "react";
import {formatFileSize, formatPercentage} from "@/components/common/utils/TextUtils";
import ServerAvailability from "@/components/servers/server-availability/ServerAvailability";

export type ServerOverviewProps = {
    server: Server
}
export default function ServerOverview(props: ServerOverviewProps) {
    if (!props.server) {
        return <Container>
            {SERVER_DETAILS_CONSTANTS.UNKNOWN}
        </Container>
    }

    return <Container
        header={<Header variant="h3">{SERVER_OVERVIEW_CONSTANTS.HEADER}</Header>}
    >
        <ColumnLayout columns={3} variant="text-grid">
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.OS_HEADER}>{getValueOrUnknown(props.server.Host?.Os?.Family)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.OS_NAME}>{getValueOrUnknown(props.server.Host?.Os?.Name)}</ValueWithLabel>
            </SpaceBetween>
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.MEMORY_HEADER}>{formatFileSize(props.server.Host?.Memory?.TotalBytes)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.CPU_HEADER}>{formatPercentage(props.server.Host?.CpuLoadAverage?.FifteenMinutes)}</ValueWithLabel>
            </SpaceBetween>
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.AVAILABILITY_HEADER}><ServerAvailability availability={props.server.Availability} unavailabilityReason={props.server.UnavailabilityReason}/></ValueWithLabel>
            </SpaceBetween>
        </ColumnLayout>
    </Container>
}
