import {
    DescribeUserGroupsSharedWithSessionTemplateRequestData,
    DescribeUsersSharedWithSessionTemplateRequestData,
    SessionTemplate,
} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import * as React from "react";
import {SpaceBetween, Tabs} from "@cloudscape-design/components";
import {TabsProps} from "@cloudscape-design/components/tabs/interfaces";
import {SESSION_TEMPLATES_DETAILS_CONSTANTS} from "@/constants/session-templates-details-constants";
import SessionTemplateOverview from "@/components/session-templates/session-template-overview/SessionTemplateOverview";
import {useEffect, useState} from "react";
import {UserGroupsState, UsersState} from "@/components/user-management/common/SplitPanelStates";
import DataAccessService from "@/components/common/utils/DataAccessService";
import UsersTab from "@/components/session-templates/session-templates-split-panel/users-tab/UsersTab";
import GroupsTab from "@/components/session-templates/session-templates-split-panel/groups-tab/GroupsTab";

export type SessionTemplatesDetailsTabsProps = {
    sessionTemplate: SessionTemplate | undefined
}

const dataAccessService = new DataAccessService()

export function SessionTemplatesDetailsTabs(props: SessionTemplatesDetailsTabsProps) {
    const [usersState, setUsersState] = useState<UsersState>({
        users: [],
        loading: true,
        error: false
    })

    const [userGroupsState, setUserGroupsState] = useState<UserGroupsState>({
        groups: [],
        loading: true,
        error: false
    })

    const getUsersForSessionTemplate = (sessionTemplate: SessionTemplate) => {
        // TODO: Paginate
        const describeUsersSharedWithSessionTemplateRequest: DescribeUsersSharedWithSessionTemplateRequestData = {
            SessionTemplateId: sessionTemplate.Id
        } as DescribeUsersSharedWithSessionTemplateRequestData

        console.log(describeUsersSharedWithSessionTemplateRequest)
        dataAccessService.describeUsersSharedWithSessionTemplate(describeUsersSharedWithSessionTemplateRequest)
            .then(r => {
                setUsersState(prevState => {
                    return {
                        ...prevState,
                        users: r.data.Users || [],
                        loading: false,
                        error: false
                    }
                })
            }).catch(e => {
            console.error("Failed to retrieve users: ", e)
            setUsersState(prevState => {
                return {
                    ...prevState,
                    loading: false,
                    error: true,
                    errorMessage: e.errorMessage
                }
            })
        })
    }

    const getGroupsForSessionTemplate = (sessionTemplate: SessionTemplate) => {
        // TODO: Paginate
        const describeUserGroupsSharedWithSessionTemplateRequest: DescribeUserGroupsSharedWithSessionTemplateRequestData = {
            SessionTemplateId: sessionTemplate.Id
        } as DescribeUserGroupsSharedWithSessionTemplateRequestData

        console.log(describeUserGroupsSharedWithSessionTemplateRequest)
        dataAccessService.describeUserGroupsSharedWithSessionTemplate(describeUserGroupsSharedWithSessionTemplateRequest)
            .then(r => {
                setUserGroupsState(prevState => {
                    return {
                        ...prevState,
                        groups: r.data.UserGroups || [],
                        loading: false,
                        error: false
                    }
                })
            }).catch(e => {
            console.error("Failed to retrieve groups: ", e)
            setUserGroupsState(prevState => {
                return {
                    ...prevState,
                    loading: false,
                    error: true,
                    errorMessage: e.errorMessage
                }
            })
        })
    }


    useEffect(() => {
        if (props.sessionTemplate) {
            getUsersForSessionTemplate(props.sessionTemplate)
            getGroupsForSessionTemplate(props.sessionTemplate)
        }
    }, [props.sessionTemplate])

    const getTabs = (sessionTemplate: SessionTemplate): TabsProps.Tab[] => {
        return [
            {
                label: SESSION_TEMPLATES_DETAILS_CONSTANTS.DETAILS,
                id: "details",
                content: <SessionTemplateOverview sessionTemplate={sessionTemplate}/>
            },
            {
                label: SESSION_TEMPLATES_DETAILS_CONSTANTS.USERS,
                id: "users",
                content: (
                    <SpaceBetween size={"m"} direction={"vertical"}>
                        <UsersTab users={usersState.users || []} sessionTemplateId={props.sessionTemplate?.Id}/>
                        <GroupsTab groups={userGroupsState.groups || []} sessionTemplateId={props.sessionTemplate?.Id}/>
                    </SpaceBetween>
                )
            },
        ]
    }

    if (!props.sessionTemplate) {
        return <Box textAlign="center" color="inherit">
            <b>{SESSION_TEMPLATES_DETAILS_CONSTANTS.NOT_FOUND}</b>
            <Box
                padding={{bottom: "s"}}
                variant="p"
                color="inherit"
            >
            </Box>
        </Box>
    }
    return <Tabs
        tabs={getTabs(props.sessionTemplate)}
    />
}
