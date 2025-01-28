// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Container, Header, Table} from "@cloudscape-design/components";
import {User} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {USERS_AND_GROUPS_TAB_CONSTANTS} from "@/constants/users-and-groups-tab-constants";
import {
    USERS_TAB_COLUMN_DEFINITIONS
} from "@/components/session-templates/session-templates-split-panel/users-tab/UsersTabColumnDefinitions";
import {USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS} from "@/constants/user-group-session-template-details-constants";
import Button from "@cloudscape-design/components/button";
import {useRouter} from "next/navigation";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";

export default function UsersTab({users, sessionTemplateId}: { users: User[], sessionTemplateId: string | undefined}) {
    const {push} = useRouter()

    return <Container>
        <Header variant={"h3"}
            actions={<Button variant="normal" onClick={() => push(SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_URL(sessionTemplateId))}>
                {SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT}
            </Button>}>
            {USERS_AND_GROUPS_TAB_CONSTANTS.USERS_TAB_TITLE}
        </Header>
        <Table
            variant="embedded"
            empty={
                <Box textAlign="center" color="inherit">
                    <b>{USERS_AND_GROUPS_TAB_CONSTANTS.EMPTY_USERS_TEXT}</b>
                    <Box
                        padding={{bottom: "s"}}
                        variant="p"
                        color="inherit"
                    >
                    </Box>
                </Box>
            }
            items={users!}
            columnDefinitions={USERS_TAB_COLUMN_DEFINITIONS}/>
    </Container>
}
