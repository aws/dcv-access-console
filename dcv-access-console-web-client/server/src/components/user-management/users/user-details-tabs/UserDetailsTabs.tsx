import {DeleteSessionUIRequestData, SessionTemplate, SessionWithPermissions, User} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {useEffect, useState} from "react";
import {Tabs} from "@cloudscape-design/components";
import {TabsProps} from "@cloudscape-design/components/tabs/interfaces";
import {USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS} from "@/constants/user-group-split-panel-details-constants";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {DeleteSessionProps} from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {USER_DETAILS_CONSTANTS} from "@/constants/user-details-constants";
import UserSessionDetails from "@/components/user-management/common/user-session-details/UserSessionDetails";
import UserOverview from "@/components/user-management/users/user-overview/UserOverview";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {useSessionsService, useSessionTemplatesService} from "@/components/common/hooks/DataAccessServiceHooks";
import UserSessionTemplateDetails
    from "@/components/user-management/common/user-session-template-details/UserSessionTemplateDetails";
import {
    unpublishSessionTemplateFromGroup,
    unpublishSessionTemplateFromUser
} from "@/components/common/utils/UserGroupUtils";
import {EDIT_USER_GROUP_CONSTANTS} from "@/constants/edit-user-group-constants";
import {useRouter} from "next/navigation";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import {USER_GROUP_SESSION_TEMPLATE_DETAILS_CONSTANTS} from "@/constants/user-group-session-template-details-constants";
import Button from "@cloudscape-design/components/button";

export type UserDetailsTabsProps = {
    user: User | undefined
}
export function getTabs(user: User): TabsProps.Tab[] {
    const {addDeletingSessionFlashBar, addDeletedSessionFailedFlashBar, addFlashBar} = useFlashBarContext()
    const [selectedSessions, setSelectedSessions] = React.useState<SessionWithPermissions[]>([])
    const [selectedSessionTemplates, setSelectedSessionTemplates] = React.useState<SessionTemplate[]>([]);
    const [refreshKey, setRefreshKey] = useState("")
    const [resetPaginationKey, setResetPaginationKey] = useState("")

    const {push} = useRouter()

    const dataAccessService = new DataAccessService()

    const userSessionsQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "States",
            operator: "!=",
            value: "DELETED"
        },
            {
                propertyKey: "UserSharedWith",
                operator: "=",
                value: user?.UserId
            }],
        operation: 'and'
    };

    const userSessionTemplatesQuery: PropertyFilterQuery = {
        tokens: [{
            propertyKey: "UsersSharedWith",
            operator: "=",
            value: user?.UserId
        }],
        operation: "and"
    }

    const getPermissionForUserAdditionProps = {
        "UserId": user?.UserId,
    } as {[key: string]: string}

    const deleteSession = ({sessionId, sessionName, owner}: DeleteSessionProps) => {
        setSelectedSessions([])
        let deleteSessionsRequest: DeleteSessionUIRequestData = {
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
        setSelectedSessions([])
        setResetPaginationKey(Date.now().toString())
    }, [user])

    return [
        {
            label: USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS.DETAILS,
            id: "details",
            content: <UserOverview user={user}/>
        },
        {
            label: USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS.SESSIONS,
            id: "sessions",
            content: <UserSessionDetails
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
        },
        {
            label: USER_GROUP_SPLIT_PANEL_DETAILS_CONSTANTS.SESSION_TEMPLATES,
            id: "sessionTemplates",
            content: <UserSessionTemplateDetails
                unpublishSessionTemplate={(sessionTemplate: SessionTemplate) => unpublishSessionTemplateFromUser(sessionTemplate, user, addFlashBar)}
                selectedSessionTemplates={selectedSessionTemplates}
                setSelectedSessionTemplates={setSelectedSessionTemplates}
                addSessionTemplate={() => push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)}
                query={userSessionTemplatesQuery}
                additionalQueryParams={getPermissionForUserAdditionProps}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                resetPaginationKey={resetPaginationKey}
                dataAccessServiceFunction={useSessionTemplatesService}
            />
        }
    ]
}

export function UserDetailsTabs({user}: UserDetailsTabsProps) {
    if (!user) {
        return <Box textAlign="center" color="inherit">
            <b>{USER_DETAILS_CONSTANTS.EMPTY_TEXT}</b>
            <Box
                padding={{bottom: "s"}}
                variant="p"
                color="inherit"
            >
            </Box>
        </Box>
    }
    return <Tabs
        tabs={getTabs(user)}
    />
}
