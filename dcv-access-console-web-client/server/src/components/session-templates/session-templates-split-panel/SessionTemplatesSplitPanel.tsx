import {SessionTemplate} from "@/generated-src/client";
import {SplitPanel} from "@cloudscape-design/components";
import * as React from "react";
import {SESSION_TEMPLATES_DETAILS_CONSTANTS} from "@/constants/session-templates-details-constants";
import Box from "@cloudscape-design/components/box";
import {SessionTemplatesDetailsTabs} from "@/components/session-templates/session-templates-details-tabs/SessionTemplatesDetailsTabs";

export default function SessionTemplatesSplitPanel({sessionTemplates}: { sessionTemplates: SessionTemplate[] }) {
    if (!sessionTemplates || sessionTemplates.length == 0) {
        return <SplitPanel header={SESSION_TEMPLATES_DETAILS_CONSTANTS.EMPTY_TEXT}
            hidePreferencesButton={true}>
            <Box textAlign="center" color="inherit">
                <b>{SESSION_TEMPLATES_DETAILS_CONSTANTS.NOT_FOUND}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        </SplitPanel>
    } else if (sessionTemplates.length > 1) {
        return <SplitPanel header={SESSION_TEMPLATES_DETAILS_CONSTANTS.MULTIPLE_SELECTED_TEXT_FN(sessionTemplates.length)}
                           hidePreferencesButton={true}>
            <Box textAlign="center" color="inherit">
                <b>{SESSION_TEMPLATES_DETAILS_CONSTANTS.MULTIPLE_SELECTED_BODY}</b>
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                </Box>
            </Box>
        </SplitPanel>
    }
    return <SplitPanel header={sessionTemplates[0].Name!}
                       i18nStrings={{
                           preferencesConfirm: "Ok",
                           preferencesCancel: "Cancel"}}>
        <SessionTemplatesDetailsTabs sessionTemplate={sessionTemplates[0]}/>
    </SplitPanel>
}
