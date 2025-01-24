import {UserGroup} from "@/generated-src/client";
import {SplitPanel} from "@cloudscape-design/components";
import * as React from "react";
import Box from "@cloudscape-design/components/box";
import {USER_GROUP_DETAILS_CONSTANTS} from "@/constants/user-group-details-constants";
import {UserGroupDetailsTabs} from "@/components/user-management/user-groups/user-group-details-tabs/UserGroupDetailsTabs";
import {SPLIT_PANEL_I18N_STRINGS} from "@/constants/split-panel-constants";

export type UserGroupsSplitPanelProps = {
    group: UserGroup | undefined,
}

export default function UserGroupsSplitPanel({
    group,
}: UserGroupsSplitPanelProps) {
    if (!group) {
        return <SplitPanel header={USER_GROUP_DETAILS_CONSTANTS.EMPTY_TEXT}
                           i18nStrings={SPLIT_PANEL_I18N_STRINGS}
        >
            <Box textAlign="center" color="inherit">
                <b>{USER_GROUP_DETAILS_CONSTANTS.NOT_FOUND}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        </SplitPanel>
    }
    return <SplitPanel header={group?.DisplayName!}
                       i18nStrings={SPLIT_PANEL_I18N_STRINGS}>
        <UserGroupDetailsTabs group={group}/>
    </SplitPanel>
}
