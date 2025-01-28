// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {SessionTemplate} from "@/generated-src/client";
import {ColumnLayout, Container, Header, SpaceBetween, Textarea} from "@cloudscape-design/components";
import {SESSION_TEMPLATES_DETAILS_CONSTANTS} from "@/constants/session-templates-details-constants";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {capitalizeFirstLetter, formatDate, formatFileSize} from "@/components/common/utils/TextUtils";
import OsLabel from "@/components/session-templates/os-label/OsLabel";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import {SESSION_TEMPLATES_TABLE_CONSTANTS} from "@/constants/session-templates-table-constants";

export default function SessionTemplateOverview({sessionTemplate}: { sessionTemplate: SessionTemplate | undefined }) {
    if (!sessionTemplate) {
        return <Box textAlign="center">{SESSION_TEMPLATES_DETAILS_CONSTANTS.EMPTY_TEXT}</Box>;
    }

    return <SpaceBetween size={"m"} direction={"vertical"}>
        <Container
            header={
                <Header
                    variant="h3"
                >
                    {SESSION_TEMPLATES_DETAILS_CONSTANTS.DETAILS}
                </Header>
            }
        >
            <ColumnLayout columns={4} variant="text-grid">
                <SpaceBetween size="l">
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.TEMPLATE_NAME}>{sessionTemplate?.Name}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.TEMPLATE_DESCRIPTION}>{sessionTemplate?.Description || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_TABLE_CONSTANTS.CREATION_TIME_HEADER}>{formatDate(sessionTemplate?.CreationTime)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_TABLE_CONSTANTS.CREATED_BY_HEADER}>{sessionTemplate?.CreatedBy}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_TIME_HEADER}>{formatDate(sessionTemplate?.LastModifiedTime)}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_BY_HEADER}>{sessionTemplate?.LastModifiedBy}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size="l">
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.OS}><OsLabel osFamily={sessionTemplate.OsFamily}/></ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION}>{sessionTemplate?.OsVersions || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.TYPE}>{capitalizeFirstLetter(sessionTemplate.Type)}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size="l">
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID}>{sessionTemplate?.InstanceIds || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE}>{sessionTemplate?.InstanceTypes || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION}>{sessionTemplate?.InstanceRegions || "Not specified"}</ValueWithLabel>
                </SpaceBetween>
                <SpaceBetween size = "l">
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS}>{sessionTemplate?.HostNumberOfCpus || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY}>{sessionTemplate?.HostMemoryTotalBytes?.split("; ").map(number => formatFileSize(number)).join("; ") || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.OPENGL}>{sessionTemplate?.DcvGlEnabled?.toString() || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_FILE}>{sessionTemplate?.AutorunFile || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_ARGUMENTS}>{sessionTemplate?.AutorunFileArguments || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.MAX_CONCURRENT_CLIENTS}>{sessionTemplate?.MaxConcurrentClients || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.STORAGE_ROOT}>{sessionTemplate?.StorageRoot || "Not specified"}</ValueWithLabel>
                    <ValueWithLabel
                        label={SESSION_TEMPLATES_CREATE_CONSTANTS.INIT_FILE}>{sessionTemplate?.InitFile || "Not specified"}</ValueWithLabel>
                </SpaceBetween>
            </ColumnLayout>
        </Container>

        <Container header={<Header variant="h3">{SESSION_TEMPLATES_DETAILS_CONSTANTS.REQUIREMENTS}</Header>}>
            <Textarea value={sessionTemplate.Requirements} readOnly/>
        </Container>
    </SpaceBetween>
}
