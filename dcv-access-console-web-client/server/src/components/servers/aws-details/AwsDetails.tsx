import {Aws} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {AWS_DETAILS_CONSTANTS} from "@/constants/aws-details-constants";
import {ColumnLayout, Container, Header, SpaceBetween} from "@cloudscape-design/components";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";

export default function AwsDetails({aws}: { aws: Aws | undefined }) {
    if (!aws) {
        return <Box textAlign="center">{AWS_DETAILS_CONSTANTS.EMPTY_TEXT}</Box>;
    }
    return <Container
        header={
            <Header
                variant="h3"
            >
                {AWS_DETAILS_CONSTANTS.AWS_HEADER}
            </Header>
        }
    >
        <ColumnLayout columns={2} variant="text-grid">
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.AWS_REGION}>{getValueOrUnknown(aws?.Region)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.AWS_EC2_INSTANCE_TYPE}>{getValueOrUnknown(aws?.EC2InstanceType)}</ValueWithLabel>
            </SpaceBetween>
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.AWS_EC2_INSTANCE_ID}>{getValueOrUnknown(aws?.EC2InstanceId)}</ValueWithLabel>
                <ValueWithLabel
                    label={SERVERS_TABLE_CONSTANTS.AWS_EC2_IMAGE_ID}>{getValueOrUnknown(aws?.EC2ImageId)}</ValueWithLabel>
            </SpaceBetween>
        </ColumnLayout>
    </Container>
}
