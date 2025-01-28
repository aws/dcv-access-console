// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {ContentLayout} from "@cloudscape-design/components";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import * as React from "react";
import EditUserGroupForm
    , {
    UserGroupFormSubmitButtonProps
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-form/EditUserGroupForm";
import {CREATE_USER_GROUP_CONSTANTS} from "@/constants/create-user-group-constants";
import {CREATE_USER_GROUP_FORM_CONSTANTS} from "@/constants/create-user-group-form-constants";
import {CreateUserGroupRequestData} from "@/generated-src/client";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";

export type CreateUserGroupProps = {
    redirectToUserGroupsPageFunction: () => void
    cancelCreateUserGroupsPageFunction: () => void
}

export default function CreateUserGroup(
    {
        redirectToUserGroupsPageFunction,
        cancelCreateUserGroupsPageFunction
    } : CreateUserGroupProps) {

    const dataAccessService = new DataAccessService()
    const {addFlashBar} = useFlashBarContext()

    const submitCreateUserGroupRequest = ({
                                              groupId,
                                              displayName,
                                              userIdsToAdd,
                                              sessionTemplateIdsToPublish,
    }: UserGroupFormSubmitButtonProps) => {

        const createUserGroupRequest: CreateUserGroupRequestData = {
            UserGroupId: groupId,
            DisplayName: displayName,
            UserIds: userIdsToAdd,
            SessionTemplateIds: sessionTemplateIdsToPublish
        }

        dataAccessService.createUserGroup(createUserGroupRequest).then(result => {
            addFlashBar("success", result.data.UserGroup?.UserGroupId, 'Successfully created user group "' + displayName + '".')
            redirectToUserGroupsPageFunction()
        }).catch(error => {
            console.log("Error while creating user group: ", error)
            addFlashBar("error", groupId, 'An error occurred while creating user group "' + groupId + '".')
        })
    }

    return (
        <ContentLayout
            header={
                <ConsoleHeader
                    headerTitle={CREATE_USER_GROUP_CONSTANTS.CREATE_USER_GROUP_HEADER_TITLE }
                    headerDescription={CREATE_USER_GROUP_CONSTANTS.CREATE_USER_GROUP_DESCRIPTION}
                />
            }
        >
            <EditUserGroupForm
                group={undefined}
                formConstants={CREATE_USER_GROUP_FORM_CONSTANTS}
                isEditPage={false}
                submitButtonFunction={submitCreateUserGroupRequest}
                cancelButtonFunction={cancelCreateUserGroupsPageFunction}
            />
        </ContentLayout>
    )
}