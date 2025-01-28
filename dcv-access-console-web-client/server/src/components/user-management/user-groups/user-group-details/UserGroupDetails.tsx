// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {DeleteSessionUIRequestData, SessionTemplate, SessionWithPermissions, UserGroup} from "@/generated-src/client";
import {Alert, AppLayout, ContentLayout, SpaceBetween, StatusIndicator} from "@cloudscape-design/components";
import * as React from "react";
import {useEffect, useState} from "react";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import Box from "@cloudscape-design/components/box";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {Session} from "next-auth";
import {USER_GROUP_DETAILS_CONSTANTS} from "@/constants/user-group-details-constants";
import UserGroupOverview from "@/components/user-management/user-groups/user-group-overview/UserGroupOverview";
import {DeleteSessionProps} from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import UserSessionTemplateDetails
    from "@/components/user-management/common/user-session-template-details/UserSessionTemplateDetails";
import UserGroupUserDetails
    from "@/components/user-management/user-groups/user-group-user-details/UserGroupUserDetails";
import {unpublishSessionTemplateFromGroup} from "@/components/common/utils/UserGroupUtils";
import {useRouter} from "next/navigation";
import {EDIT_USER_GROUP_CONSTANTS} from "@/constants/edit-user-group-constants";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {useSessionTemplatesService, useUsersService} from "@/components/common/hooks/DataAccessServiceHooks";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export type UserGroupDetailsProps = {
    group?: UserGroup,
    loading: boolean,
    error: boolean,
}
export default function UserGroupDetails(props: UserGroupDetailsProps) {
    const dataAccessService = new DataAccessService()
    const {addDeletingSessionFlashBar, addDeletedSessionFailedFlashBar, addFlashBar} = useFlashBarContext()
    const {push} = useRouter();

    const [selectedSessions, setSelectedSessions] = React.useState<SessionWithPermissions[]>([]);
    const [selectedSessionTemplates, setSelectedSessionTemplates] = React.useState<SessionTemplate[]>([]);
    const [refreshKey, setRefreshKey] = useState("")
    const [resetPaginationKey, setResetPaginationKey] = useState("")

    const getPermissionForGroupAdditionProps = {
        "UserGroupId": props.group?.UserGroupId,
    } as {[key: string]: string}

    const userSessionTemplatesQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "GroupsSharedWith",
            operator: "=",
            value: props.group?.UserGroupId
        }],
        operation: "and"
    }

    const usersInGroupQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "UserGroupIds",
            operator: "=",
            value: props.group?.UserGroupId
        }],
        operation: "and"
    }

    const deleteSession = ({sessionId, sessionName, owner}: DeleteSessionProps) => {
        setSelectedSessions([])
        const deleteSessionsRequest: DeleteSessionUIRequestData = {
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

    useEffect(() => {
        setRefreshKey(Date.now().toString())
    }, [useFlashBarContext().items])

    useEffect(() => {
        setSelectedSessions([])
        setSelectedSessionTemplates([])
        setResetPaginationKey(Date.now().toString())
    }, [props.group])

    let content

    if (props.loading) {
        content = <ContentLayout>
                <SpaceBetween size={"l"} direction={"vertical"}>
                    <Box textAlign="center" color="inherit">
                        <StatusIndicator type="loading">
                            {USER_GROUP_DETAILS_CONSTANTS.LOADING_TEXT}
                        </StatusIndicator>
                    </Box>
                </SpaceBetween>
            </ContentLayout>
    } else if (props.error) {
        content = <ContentLayout>
                <Box textAlign="center" color="inherit">
                    <StatusIndicator type="error">
                        {USER_GROUP_DETAILS_CONSTANTS.ERROR_TEXT}
                    </StatusIndicator>
                </Box>
            </ContentLayout>
    } else {
        content = <ContentLayout
            header={<HeaderWithCounter>{getValueOrUnknown(props.group?.DisplayName)}</HeaderWithCounter>}
        >
                <SpaceBetween size={"l"}>
                    <UserGroupOverview group={props.group!}/>
                    <UserGroupUserDetails
                        query={usersInGroupQuery}
                        refreshKey={refreshKey}
                        setRefreshKey={setRefreshKey}
                        resetPaginationKey={resetPaginationKey}
                        dataAccessServiceFunction={useUsersService}
                    />
                    {/*<UserSessionDetails sessionsState={sessionState} deleteSession={deleteSession} selectedItems={selectedSessions} setSelectedItems={setSelectedSessions}/>*/}
                    <UserSessionTemplateDetails
                        unpublishSessionTemplate={(sessionTemplate: SessionTemplate) => unpublishSessionTemplateFromGroup(sessionTemplate, props.group!, addFlashBar)}
                        addSessionTemplate={() => push(EDIT_USER_GROUP_CONSTANTS.EDIT_USER_GROUP_URL(props.group?.UserGroupId))}
                        selectedSessionTemplates={selectedSessionTemplates} setSelectedSessionTemplates={setSelectedSessionTemplates}
                        query={userSessionTemplatesQuery}
                        additionalQueryParams={getPermissionForGroupAdditionProps}
                        refreshKey={refreshKey}
                        setRefreshKey={setRefreshKey}
                        resetPaginationKey={resetPaginationKey}
                        dataAccessServiceFunction={useSessionTemplatesService}
                    />
                </SpaceBetween>
            </ContentLayout>
        }
    return content
}
