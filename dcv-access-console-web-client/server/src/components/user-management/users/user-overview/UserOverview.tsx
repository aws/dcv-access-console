import {User} from "@/generated-src/client";
import {ColumnLayout, Container, SpaceBetween} from "@cloudscape-design/components";
import {ValueWithLabel} from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import * as React from "react";
import {formatDate} from "@/components/common/utils/TextUtils";
import {USER_DETAILS_CONSTANTS} from "@/constants/user-details-constants";

export type UserOverviewProps = {
    user: User
}
export default function UserOverview({user}: UserOverviewProps) {
    if (!user) {
        return <Container>
            {USER_DETAILS_CONSTANTS.UNKNOWN}
        </Container>
    }

    return <Container>
        <ColumnLayout columns={2} variant="text-grid">
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={USER_DETAILS_CONSTANTS.DISPLAY_NAME_HEADER}>{getValueOrUnknown(user.DisplayName)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_DETAILS_CONSTANTS.ID_HEADER}>{getValueOrUnknown(user.UserId)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_DETAILS_CONSTANTS.ROLE_HEADER}>{getValueOrUnknown(user.Role)}</ValueWithLabel>
            </SpaceBetween>
            <SpaceBetween size="l">
                <ValueWithLabel
                    label={USER_DETAILS_CONSTANTS.LAST_TIME_ACTIVE_HEADER}>{formatDate(user.LastLoggedInTime)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_DETAILS_CONSTANTS.DATE_CREATED_HEADER}>{formatDate(user.CreationTime)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_DETAILS_CONSTANTS.DATE_MODIFIED_HEADER}>{formatDate(user.LastModifiedTime)}</ValueWithLabel>
                <ValueWithLabel
                    label={USER_DETAILS_CONSTANTS.IS_IMPORTED_HEADER}>{user.IsImported ? USER_DETAILS_CONSTANTS.YES : USER_DETAILS_CONSTANTS.NO}</ValueWithLabel>
            </SpaceBetween>
        </ColumnLayout>
    </Container>
}
