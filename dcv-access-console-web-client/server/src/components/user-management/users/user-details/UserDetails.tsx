// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {DeleteSessionUIRequestData, SessionTemplate, SessionWithPermissions, User} from "@/generated-src/client";
import {ContentLayout, SpaceBetween, StatusIndicator} from "@cloudscape-design/components";
import * as React from "react";
import {useEffect, useState} from "react";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import Box from "@cloudscape-design/components/box";
import {USER_DETAILS_CONSTANTS} from "@/constants/user-details-constants";
import UserOverview from "@/components/user-management/users/user-overview/UserOverview";
import {DeleteSessionProps} from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import UserSessionTemplateDetails
    from "@/components/user-management/common/user-session-template-details/UserSessionTemplateDetails";
import {unpublishSessionTemplateFromUser} from "@/components/common/utils/UserGroupUtils";
import {useRouter} from "next/navigation";
import UserSessionDetails from "@/components/user-management/common/user-session-details/UserSessionDetails";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {useSessionsService, useSessionTemplatesService} from "@/components/common/hooks/DataAccessServiceHooks";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export type UserDetailsProps = {
    user?: User,
    loading: boolean,
    error: boolean,
}
export default function UserDetails(props: UserDetailsProps) {
    const dataAccessService = new DataAccessService()
    const {addDeletingSessionFlashBar, addDeletedSessionFailedFlashBar, addFlashBar} = useFlashBarContext()
    const {push} = useRouter()

    const [selectedSessions, setSelectedSessions] = useState<SessionWithPermissions[]>([]);
    const [selectedSessionTemplates, setSelectedSessionTemplates] = useState<SessionTemplate[]>([]);

    const [refreshKey, setRefreshKey] = useState("")
    const [resetPaginationKey, setResetPaginationKey] = useState("")

    const userSessionsQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "States",
            operator: "!=",
            value: "DELETED"
        },
        {
            propertyKey: "UserSharedWith",
            operator: "=",
            value: props.user?.UserId
        }],
        operation: 'and'
    };

    const getPermissionForUserAdditionProps = {
        "UserId": props.user?.UserId,
    } as {[key: string]: string}

    const userSessionTemplatesQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "UsersSharedWith",
            operator: "=",
            value: props.user?.UserId
        }],
        operation: "and"
    }

    const deleteSession = ({sessionId, sessionName, owner}: DeleteSessionProps) => {
        setSelectedSessions([])
        const deleteSessionsRequest: DeleteSessionUIRequestData = {
            SessionId: sessionId,
            Owner: owner
        } as DeleteSessionUIRequestData
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
    }, [props.user])

    let content

    if (props.loading) {
        content = <ContentLayout>
            <SpaceBetween size={"l"} direction={"vertical"}>
                <Box textAlign="center" color="inherit">
                    <StatusIndicator type="loading">
                        {USER_DETAILS_CONSTANTS.LOADING_TEXT}
                    </StatusIndicator>
                </Box>
            </SpaceBetween>
        </ContentLayout>
    } else if (props.error) {
        content =  <ContentLayout>
            <Box textAlign="center" color="inherit">
                <StatusIndicator type="error">
                    {USER_DETAILS_CONSTANTS.ERROR_TEXT}
                </StatusIndicator>
            </Box>
        </ContentLayout>
    } else {
        content = <ContentLayout
            header={<HeaderWithCounter>{getValueOrUnknown(props.user?.DisplayName)}</HeaderWithCounter>}
        >
            <SpaceBetween size={"l"}>
                <UserOverview user={props.user!}/>
                <UserSessionDetails
                    deleteSession={deleteSession}
                    selectedItems={selectedSessions}
                    setSelectedItems={setSelectedSessions}
                    query={userSessionsQuery}
                    additionalQueryParams={getPermissionForUserAdditionProps}
                    refreshKey={refreshKey}
                    setRefreshKey={setRefreshKey}
                    resetPaginationKey={resetPaginationKey}
                    dataAccessServiceFunction={useSessionsService}
                />
                <UserSessionTemplateDetails
                    unpublishSessionTemplate={(sessionTemplate: SessionTemplate) => unpublishSessionTemplateFromUser(sessionTemplate, props.user!, addFlashBar)}
                    selectedSessionTemplates={selectedSessionTemplates} setSelectedSessionTemplates={setSelectedSessionTemplates}
                    addSessionTemplate={() => push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)}
                    query={userSessionTemplatesQuery}
                    additionalQueryParams={getPermissionForUserAdditionProps}
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
