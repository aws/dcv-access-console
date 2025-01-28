// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {User} from "@/generated-src/client";
import {SplitPanel} from "@cloudscape-design/components";
import * as React from "react";
import Box from "@cloudscape-design/components/box";
import {SPLIT_PANEL_I18N_STRINGS} from "@/constants/split-panel-constants";
import {UserDetailsTabs} from "@/components/user-management/users/user-details-tabs/UserDetailsTabs";
import {USER_DETAILS_CONSTANTS} from "@/constants/user-details-constants";

export type UsersSplitPanelProps = {
    user: User | undefined,
}

export default function UsersSplitPanel({
    user,
}: UsersSplitPanelProps) {
    if (!user) {
        return <SplitPanel header={USER_DETAILS_CONSTANTS.EMPTY_TEXT}
                           i18nStrings={SPLIT_PANEL_I18N_STRINGS}
        >
            <Box textAlign="center" color="inherit">
                <b>{USER_DETAILS_CONSTANTS.NOT_FOUND}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        </SplitPanel>
    }
    return <SplitPanel header={user?.DisplayName!}
                       i18nStrings={SPLIT_PANEL_I18N_STRINGS}>
        <UserDetailsTabs user={user}/>
    </SplitPanel>
}
