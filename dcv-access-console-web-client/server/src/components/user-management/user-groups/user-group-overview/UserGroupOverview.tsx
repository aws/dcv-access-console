import {UserGroup} from "@/generated-src/client";
import {ColumnLayout, Container, SpaceBetween} from "@cloudscape-design/components";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import * as React from "react";
import {formatDate} from "@/components/common/utils/TextUtils";
import {USER_GROUP_DETAILS_CONSTANTS} from "@/constants/user-group-details-constants";

export type UserGroupOverviewProps = {
    group: UserGroup
}
export default function UserGroupOverview({group}: UserGroupOverviewProps) {
    if (!group) {
        return <Container>
            {USER_GROUP_DETAILS_CONSTANTS.UNKNOWN}
        </Container>
    }

    return <Container>
        <ColumnLayout columns={2} variant="text-grid">
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={USER_GROUP_DETAILS_CONSTANTS.NAME_HEADER}>{getValueOrUnknown(group.DisplayName)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_GROUP_DETAILS_CONSTANTS.ID_HEADER}>{getValueOrUnknown(group.UserGroupId)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_GROUP_DETAILS_CONSTANTS.NUMBER_OF_USERS_HEADER}>{String(group.UserIds?.length || 0)}</ValueWithLabel>
            </SpaceBetween>
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={USER_GROUP_DETAILS_CONSTANTS.DATE_CREATED_HEADER}>{formatDate(group.CreationTime)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_GROUP_DETAILS_CONSTANTS.DATE_MODIFIED_HEADER}>{formatDate(group.LastModifiedTime)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_GROUP_DETAILS_CONSTANTS.IS_IMPORTED_HEADER}>{group.IsImported ? USER_GROUP_DETAILS_CONSTANTS.YES : USER_GROUP_DETAILS_CONSTANTS.NO}</ValueWithLabel>
            </SpaceBetween>
        </ColumnLayout>
    </Container>
}
