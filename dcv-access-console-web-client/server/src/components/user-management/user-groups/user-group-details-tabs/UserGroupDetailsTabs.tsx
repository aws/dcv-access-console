// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {DeleteSessionUIRequestData, SessionTemplate, SessionWithPermissions, UserGroup} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {useEffect, useState} from "react";
import {Tabs} from "@cloudscape-design/components";
import {TabsProps} from "@cloudscape-design/components/tabs/interfaces";
import {USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS} from "@/constants/user-group-split-panel-details-constants";
import UserSessionTemplateDetails
    from "@/components/user-management/common/user-session-template-details/UserSessionTemplateDetails";
import UserGroupUserDetails
    from "@/components/user-management/user-groups/user-group-user-details/UserGroupUserDetails";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {DeleteSessionProps} from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {unpublishSessionTemplateFromGroup} from "@/components/common/utils/UserGroupUtils";
import {useRouter} from "next/navigation";
import {USER_GROUP_DETAILS_CONSTANTS} from "@/constants/user-group-details-constants";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {useSessionTemplatesService, useUsersService} from "@/components/common/hooks/DataAccessServiceHooks";
import {EDIT_USER_GROUP_CONSTANTS} from "@/constants/edit-user-group-constants";

export type UserGroupDetailsTabsProps = {
    group: UserGroup | undefined,

}
export function getTabs(group: UserGroup): TabsProps.Tab[] {
    const {addDeletingSessionFlashBar, addDeletedSessionFailedFlashBar, addFlashBar} = useFlashBarContext()
    const [selectedSessions, setSelectedSessions] = React.useState<SessionWithPermissions[]>([]);
    const [selectedSessionTemplates, setSelectedSessionTemplates] = React.useState<SessionTemplate[]>([]);
    const [refreshKey, setRefreshKey] = useState("")
    const [resetPaginationKey, setResetPaginationKey] = useState("")

    const {push} = useRouter()
    const dataAccessService = new DataAccessService()

    const getPermissionForGroupAdditionProps = {
        "UserGroupId": group?.UserGroupId,
    } as {[key: string]: string}

    const groupSessionTemplatesQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "GroupsSharedWith",
            operator: "=",
            value: group?.UserGroupId
        }],
        operation: "and"
    }

    const usersInGroupQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "UserGroupIds",
            operator: "=",
            value: group?.UserGroupId
        }],
        operation: "and"
    }

    const deleteSession = ({sessionId, sessionName, owner}: DeleteSessionProps) => {
        setSelectedSessions([])
        let deleteSessionsRequest: DeleteSessionUIRequestData = {
            SessionId: sessionId,
            Owner: owner
        }
        dataAccessService.deleteSessions([deleteSessionsRequest]).then(r => {
            console.log("Deleting session: ", r);
            if (r.data["Error"]?.message) {
                addDeletedSessionFailedFlashBar(sessionId!, sessionName!, r.data["Error"].message)
            }
            if (r.data["SuccessfulList"]) {
                for (const success of r.data["SuccessfulList"]) {
                    addDeletingSessionFlashBar(success.SessionId!, sessionName!);
                }
            }
            if (r.data["UnsuccessfulList"]) {
                for (const failure of r.data["UnsuccessfulList"]) {
                    addDeletedSessionFailedFlashBar(failure.SessionId!, sessionName!, JSON.stringify(failure.FailureReasons))
                }
            }
        }).catch(e => {
            addDeletedSessionFailedFlashBar(sessionId!, sessionName!, "Unknown error")
            console.error("Failed to delete session: ", sessionId, e);
        })
    }

    // We get all the information for the split panel whenever the selected Group changes. This way the user doesn't
    // have to wait once they click a tab. This also means that we can reuse the tabs in the User split panel.
    useEffect(() => {
        setSelectedSessions([])
        setSelectedSessionTemplates([])
        setResetPaginationKey(Date.now().toString())
        // Uncomment once Session Sharing is enabled
        // group ? getSessionsForGroup(group) : undefined
    }, [group])

    return [
        {
            label: USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS.USERS,
            id: "users",
            content: <UserGroupUserDetails
                query={usersInGroupQuery}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                resetPaginationKey={resetPaginationKey}
                dataAccessServiceFunction={useUsersService}
            />
        },
        // Sessions Tab disabled until Session Sharing is enabled
        // {
        //     label: USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS.SESSIONS,
        //     id: "sessions",
        //     content: <UserSessionDetails
        //             deleteSession={deleteSession}
        //             sessionsState={sessionState}
        //             selectedItems={selectedSessions}
        //             setSelectedItems={setSelectedSessions}
        //         />
        // },
        {
            label: USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS.SESSION_TEMPLATES,
            id: "sessionTemplates",
            content: <UserSessionTemplateDetails
                unpublishSessionTemplate={(sessionTemplate: SessionTemplate) => unpublishSessionTemplateFromGroup(sessionTemplate, group, addFlashBar)}
                selectedSessionTemplates={selectedSessionTemplates}
                setSelectedSessionTemplates={setSelectedSessionTemplates}
                addSessionTemplate={() => push(EDIT_USER_GROUP_CONSTANTS.EDIT_USER_GROUP_URL(group.UserGroupId))}
                query={groupSessionTemplatesQuery}
                additionalQueryParams={getPermissionForGroupAdditionProps}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                resetPaginationKey={resetPaginationKey}
                dataAccessServiceFunction={useSessionTemplatesService}
            />
        },
    ]
}

export function UserGroupDetailsTabs({group}: UserGroupDetailsTabsProps) {
    if (!group) {
        return <Box textAlign="center" color="inherit">
            <b>{USER_GROUP_DETAILS_CONSTANTS.EMPTY_TEXT}</b>
            <Box
                padding={{bottom: "s"}}
                variant="p"
                color="inherit"
            >
            </Box>
        </Box>
    }
    return <Tabs
        tabs={getTabs(group)}
    />
}
