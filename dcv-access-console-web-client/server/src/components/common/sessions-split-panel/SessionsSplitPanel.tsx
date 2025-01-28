// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {
    Box,
    ColumnLayout,
    Container,
    Header,
    SpaceBetween,
    SplitPanel,
    Table,
    TextFilter
} from "@cloudscape-design/components";
import * as React from "react";
import Button from "@cloudscape-design/components/button";
import {SessionWithPermissions} from "@/generated-src/client";
import {SESSION_CARDS_DETAILS_CONSTANTS} from "@/constants/session-cards-details-constants";
import StatusLabel from "@/components/sessions/sessions-cards/status-label/StatusLabel";
import {
    capitalizeFirstLetter,
    formatDate,
    formatFileSize,
    formatUserOverTotal
} from "@/components/common/utils/TextUtils";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";

export default function SessionsSplitPanel({selectedSession}: {selectedSession: SessionWithPermissions | undefined}) {
    if (!selectedSession) {
        return <SplitPanel header={SESSION_CARDS_DETAILS_CONSTANTS.EMPTY_TEXT}
            hidePreferencesButton={true}>
            <Box textAlign="center" color="inherit">
                <b>{SESSION_CARDS_DETAILS_CONSTANTS.NOT_FOUND}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        </SplitPanel>
    }
    return (
        <SplitPanel header={selectedSession?.Name!}
            i18nStrings={{
                preferencesConfirm: "Ok",
                preferencesCancel: "Cancel"}}>
            {selectedSession? <DetailsTabContent session={selectedSession} /> : undefined}
        </SplitPanel>
    )
}

export const ValueWithLabel = ({ label, children }: {label: string, children: JSX.Element | string}) => (
    <div>
        <Box variant="awsui-key-label">{label}</Box>
        <div>{children}</div>
    </div>
)

function DetailsTabContent({session}: {session: SessionWithPermissions}) {
    const lastDisconnectionTime = formatDate(session.LastDisconnectionTime)
    const creationTime = formatDate(session.CreationTime)
    return (
        <Container header={<Header headingTagOverride="h3">Details</Header>}>
            <ColumnLayout columns={4} variant="text-grid">
                <SpaceBetween size="l">
                    <ValueWithLabel label={SESSIONS_CONSTANTS.SESSION_NAME}>{getValueOrUnknown(session.Name)}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.SESSION_LEVEL_OF_ACCESS}>{getValueOrUnknown(session.LevelOfAccess)}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.STATUS_HEADER}><StatusLabel state={session.State}/></ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.SESSION_OWNER}>{getValueOrUnknown(session.Owner)}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size="l">
                    <ValueWithLabel label={SESSIONS_CONSTANTS.ID_HEADER}>{getValueOrUnknown(session.Id)}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.HOSTNAME_HEADER}>{getValueOrUnknown(session.Server?.Hostname)}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.IP_ADDRESS_HEADER}>{getValueOrUnknown(session.Server?.Ip)}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.OS_HEADER}>{capitalizeFirstLetter(getValueOrUnknown(session.Server?.Host?.Os?.Family?.toString()))}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size="l">
                    <ValueWithLabel label={SESSIONS_CONSTANTS.CPU_HEADER}>{getValueOrUnknown(session.Server?.Host?.CpuInfo?.ModelName)}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.GPU_HEADER}>{getValueOrUnknown(session.Server?.Host?.Gpus?.map(gpu => gpu.ModelName).join(";"))}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.MEMORY}>{formatUserOverTotal(formatFileSize(session.Server?.Host?.Memory?.UsedBytes), formatFileSize(session.Server?.Host?.Memory?.TotalBytes))}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size={"l"}>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.LAST_TIME_CONNECTED_HEADER}>{lastDisconnectionTime == "Invalid date" ? SERVER_DETAILS_CONSTANTS.UNKNOWN : lastDisconnectionTime}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.NUM_OF_CONNECTIONS_HEADER}>{session.NumOfConnections?.toString() || "0"}</ValueWithLabel>
                    <ValueWithLabel label={SESSIONS_CONSTANTS.CREATION_TIME_HEADER}>{creationTime == "Invalid date" ? SERVER_DETAILS_CONSTANTS.UNKNOWN : creationTime}</ValueWithLabel>
                </SpaceBetween>
            </ColumnLayout>
        </Container>
    );
}

function UsersTabContent() {
    const [
        selectedItems,
        setSelectedItems
    ] = React.useState([]);

    const [
        users,
        setUsers
    ] = React.useState([
        {
            name: "Paolo",
            alt: "Paolo",
            email: "paolog@amazon.com",
            last_active: "Currently active",
        },
        {
            name: "Jeff",
            alt: "User2",
            email: "jeff@amazon.com",
            last_active: "Yesterday",
        },
        {
            name: "Andy",
            alt: "Andy",
            email: "ajassy@amazon.com",
            last_active: "Never",
        }
    ])
    return (
        <Table
            onSelectionChange={({ detail }) =>
                setSelectedItems(detail.selectedItems)
            }
            selectedItems={selectedItems}
            ariaLabels={{
                selectionGroupLabel: "Items selection",
                allItemsSelectionLabel: ({ selectedItems }) =>
                    `${selectedItems.length} ${
                        selectedItems.length === 1 ? "item" : "items"
                    } selected`,
                itemSelectionLabel: ({ selectedItems }, item) => {
                    const isItemSelected = selectedItems.filter(
                        i => i.name === item.name
                    ).length;
                    return `${item.name} is ${
                        isItemSelected ? "" : "not"
                    } selected`;
                }
            }}
            columnDefinitions={[
                {
                    id: "role",
                    header: "Role",
                    cell: e => e.name,
                    sortingField: "name",
                    isRowHeader: true,
                    width: 300,
                    minWidth: 100
                },
                {
                    id: "email",
                    header: "Email",
                    cell: e => e.email,
                    sortingField: "email",
                    width: 400,
                    minWidth: 100
                },
                {
                    id: "last_active",
                    header: "Last time active",
                    cell: e => e.last_active,
                    sortingField: "last_active",
                    width: 300,
                    minWidth: 100
                }
            ]}
            resizableColumns
            columnDisplay={[
                { id: "role", visible: true },
                { id: "email", visible: true },
                { id: "last_active", visible: true }
            ]}
            items={users}
            loadingText="Loading resources"
            selectionType="multi"
            trackBy="name"
            empty={
                <Box textAlign="center" color="inherit">
                    <b>No resources</b>
                    <Box
                        padding={{ bottom: "s" }}
                        variant="p"
                        color="inherit"
                    >
                        No users found.
                    </Box>
                    <Button>Create user</Button>
                </Box>
            }
            filter={
                <TextFilter
                    filteringPlaceholder="Find users"
                    filteringText=""
                />
            }
            header={
                <Header
                    counter={
                        selectedItems.length
                            ? "(" + selectedItems.length + "/" + users.length + ")"
                            : "" + users.length
                    }
                >
                    Users
                </Header>
            }
        />
    );
}
