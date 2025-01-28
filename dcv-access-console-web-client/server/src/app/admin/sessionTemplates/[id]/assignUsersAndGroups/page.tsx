// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {
    AppLayout,
    Container,
    ContentLayout, Flashbar,
    FormField,
    Header,
    SpaceBetween
} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import {useEffect, useRef, useState} from "react";
import Button from "@cloudscape-design/components/button";
import {useRouter} from "next/navigation";
import Box from "@cloudscape-design/components/box";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import {
    FilterTokenOperatorEnum,
    PublishSessionTemplateRequestData, User
} from "@/generated-src/client";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {getCleanArray} from "@/components/common/utils/TextUtils";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import {OptionDefinition} from "@cloudscape-design/components/internal/components/option/interfaces";
import {
    SelectStateType
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-session-templates/EditUserGroupSessionTemplates";
import AssignUsersGroups from "@/components/session-templates/assign-users-groups/AssignUsersGroups";

let sessionTemplateName = undefined

export function publishSessionTemplate(sessionTemplateId: string, users: [string], groups: [string]) {
    const clean_users = getCleanArray(users)
    const clean_groups = getCleanArray(groups)
    const publishSessionTemplateRequestData: PublishSessionTemplateRequestData = {
        Id: sessionTemplateId,
        UserIds: clean_users,
        GroupIds: clean_groups
    } as PublishSessionTemplateRequestData

    const dataService = new DataAccessService()
    console.log("publishSessionTemplateRequest: ", publishSessionTemplateRequestData)
    return dataService.publishSessionTemplate(publishSessionTemplateRequestData)
}

export default function AssignUsers({params}: { params: { id: string } }) {
    const [errorUsers, setErrorUsers] = useState()
    const [users, setUsers] = React.useState([null])
    const [groups, setGroups] = React.useState([null])
    const [loading, setLoading] = React.useState(false)
    const {items, addFlashBar} = useFlashBarContext()
    const {loading: pageLoading, userSession} = usePageLoading()
    const {push} = useRouter()

    useEffect(() => {
        setLoading(true)
        new DataAccessService().describeSessionTemplates({
            Ids: [{
                Operator: FilterTokenOperatorEnum.Equal,
                Value: params.id
            }]
        }).then(result => {
            if (result.data.SessionTemplates?.length != 1) {
                addFlashBar("error", params.id, SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_ERROR)
                push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
            } else {
                sessionTemplateName = result.data.SessionTemplates[0].Name
            }
        }).catch(result => {
            addFlashBar("error", params.id, SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_ERROR)
            push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
        })
        setLoading(false)
    }, [])

    const assignUsersContainer = <Container>
        <FormField errorText={errorUsers}>
            <AssignUsersGroups
                sessionTemplateId={params.id}
                handleUsersChange={(users: [OptionDefinition]) => {
                    let userIds: [string] = []
                    users.forEach(user => {
                        if (user.value != null) {
                            userIds.push(user.value)
                        }
                    })
                    setUsers(userIds)
                }}
                handleGroupsChange={(groups: [OptionDefinition]) => {
                    let groupIds: [string] = []
                    groups?.forEach(group => {
                        if(group.value != null) {
                            groupIds.push(group.value)
                        }
                    })
                    setGroups(groupIds)
                }}
                handleError={(error: string) => {
                    addFlashBar("error", params.id, 'Error while retrieving users and/or groups assigned to "' + sessionTemplateName + '".')
                }}
            />
        </FormField>
    </Container>

    useEffect(() => {
        if(getCleanArray(users).length || getCleanArray(groups).length) {
            setErrorUsers(undefined)
        }
    }, [users, groups])

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                toolsHide={true}
                breadcrumbs={sessionTemplateName ? <Breadcrumb id={params.id} name={sessionTemplateName}/> : undefined}
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={
                    <ContentLayout
                        header={
                            <ConsoleHeader headerTitle={SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS}
                                           headerDescription={SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_DESCRIPTION}
                            />
                        }>
                        <SpaceBetween size={"m"}>
                            {assignUsersContainer}
                            <Box float="right">
                                <SpaceBetween direction="horizontal" size="xs">
                                    <Button onClick={() => {push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
                                    }}>Cancel</Button>
                                    <Button variant={"primary"} disabled={!getCleanArray(users)?.length && !getCleanArray(groups)?.length} onClick={() => {
                                        setLoading(true)
                                        publishSessionTemplate(params.id, users, groups).then(result => {
                                            console.log("SessionTemplate published", result.data)
                                            if (result.data.UnsuccessfulUsersList?.length > 0) {
                                                addFlashBar("error", params.id + "errorUsers", 'Error while assigning users [' + getCleanArray(result.data.UnsuccessfulUsersList!) + '] to "' + sessionTemplateName + '".')
                                            }
                                            if (result.data.UnsuccessfulGroupsList?.length > 0) {
                                                addFlashBar("error", params.id + "errorGroups", 'Error while assigning groups [' + getCleanArray(result.data.UnsuccessfulGroupsList!) + '] to "' + sessionTemplateName + '".')
                                            }
                                            if ((!result.data.UnsuccessfulUsersList || !result.data.UnsuccessfulUsersList?.length)  && ((!result.data.UnsuccessfulGroupsList || !result.data.UnsuccessfulGroupsList?.length))) {
                                                addFlashBar("success", params.id + "success", 'Successfully assigned users and/or groups to "' + sessionTemplateName + '".')
                                                push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
                                            }
                                        }).catch(e => {
                                            console.error("Failed to publish sessionTemplate: ", e)
                                            addFlashBar("error", params.id, 'Error while assigning users and/or groups to "' + sessionTemplateName + '".')
                                        }).finally(() => {
                                            setLoading(false)
                                            }
                                        )
                                    }}
                                            loading={loading}>{SESSION_TEMPLATES_CREATE_CONSTANTS.SAVE}</Button>
                                </SpaceBetween>
                            </Box>
                        </SpaceBetween>
                    </ContentLayout>
                }
            >
            </AppLayout>
        </div>
    )
}
