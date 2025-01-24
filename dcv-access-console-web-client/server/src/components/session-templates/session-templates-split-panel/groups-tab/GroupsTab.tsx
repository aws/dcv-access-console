import {Container, Header, Table} from "@cloudscape-design/components";
import {UserGroup} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {USERS_AND_GROUPS_TAB_CONSTANTS} from "@/constants/users-and-groups-tab-constants";
import {
    GROUPS_TAB_COLUMN_DEFINITIONS
} from "@/components/session-templates/session-templates-split-panel/groups-tab/GroupsTabColumnDefinitions";
import Button from "@cloudscape-design/components/button";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import {useRouter} from "next/navigation";

export default function GroupsTab({groups, sessionTemplateId}: { groups: UserGroup[], sessionTemplateId: string | undefined}) {
    const {push} = useRouter()

    return <Container>
        <Header variant={"h3"}
                actions={<Button variant="normal" onClick={() => push(SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_URL(sessionTemplateId))}>
                    {SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT}
                </Button>}>
            {USERS_AND_GROUPS_TAB_CONSTANTS.GROUPS_TAB_TITLE}
        </Header>
        <Table
            variant="embedded"
            empty={
                <Box textAlign="center" color="inherit">
                    <b>{USERS_AND_GROUPS_TAB_CONSTANTS.EMPTY_GROUPS_TEXT}</b>
                    <Box
                        padding={{bottom: "s"}}
                        variant="p"
                        color="inherit"
                    >
                    </Box>
                </Box>
            }
            items={groups!}
            columnDefinitions={GROUPS_TAB_COLUMN_DEFINITIONS}/>
    </Container>
}
