import {EditUserGroupRequestData, UserGroup} from "@/generated-src/client";
import {ContentLayout, SpaceBetween, StatusIndicator} from "@cloudscape-design/components";
import Box from "@cloudscape-design/components/box";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import * as React from "react";
import {EDIT_USER_GROUP_CONSTANTS} from "@/constants/edit-user-group-constants";
import EditUserGroupForm
    , {
    UserGroupFormSubmitButtonProps
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-form/EditUserGroupForm";
import {EDIT_USER_GROUP_FORM_CONSTANTS} from "@/constants/edit-user-group-form-constants";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export type EditUserGroupProps = {
    group?: UserGroup,
    loading: boolean
    error: boolean,
    redirectToUserGroupsPageFunction: () => void,
    cancelEditUserGroupsPageFunction: () => void,
}

export default function EditUserGroup(
    {
        group,
        loading,
        error,
        redirectToUserGroupsPageFunction,
        cancelEditUserGroupsPageFunction
    }: EditUserGroupProps) {

    const dataAccessService = new DataAccessService()
    const {addFlashBar} = useFlashBarContext()
    const submitEditUserGroupRequest = ({
                                            groupId,
                                            displayName,
                                            userIdsToAdd,
                                            userIdsToRemove,
                                            sessionTemplateIdsToPublish,
                                            sessionTemplateIdsToUnpublish
                                        }: UserGroupFormSubmitButtonProps) => {

        const editUserGroupRequest: EditUserGroupRequestData = {
            UserGroupId: group?.UserGroupId,
            DisplayName: displayName,
            UserIdsToAdd: userIdsToAdd,
            UserIdsToRemove: userIdsToRemove,
            SessionTemplateIdsToAdd: sessionTemplateIdsToPublish,
            SessionTemplateIdsToRemove: sessionTemplateIdsToUnpublish,
        } as EditUserGroupRequestData

        dataAccessService.editUserGroup(editUserGroupRequest).then(result => {
            addFlashBar("success", result.data.UserGroup?.UserGroupId, "Successfully edited user group: " + result.data.UserGroup?.DisplayName)
            redirectToUserGroupsPageFunction()
        }).catch(error => {
            console.log("Error while editing user group", error)
            addFlashBar("error", group?.UserGroupId, "Error while editing User Group " + group?.UserGroupId)
        })
    }


    if (loading) {
        return <ContentLayout>
            <SpaceBetween size={"l"} direction={"vertical"}>
                <Box textAlign="center" color="inherit">
                    <StatusIndicator type="loading">
                        {EDIT_USER_GROUP_CONSTANTS.LOADING_TEXT}
                    </StatusIndicator>
                </Box>
            </SpaceBetween>
        </ContentLayout>
    }
    if (error) {
        return <ContentLayout>
            <Box textAlign="center" color="inherit">
                <StatusIndicator type="error">
                    {EDIT_USER_GROUP_CONSTANTS.ERROR_TEXT}
                </StatusIndicator>
            </Box>
        </ContentLayout>
    }
    return (
        <ContentLayout
            header={
            <HeaderWithCounter
                description={EDIT_USER_GROUP_CONSTANTS.DESCRIPTION_TEXT}>
                {EDIT_USER_GROUP_CONSTANTS.EDIT_STRING + " \"" + group?.DisplayName + "\""}
            </HeaderWithCounter>}
        >
            <EditUserGroupForm
                group={group!}
                formConstants={EDIT_USER_GROUP_FORM_CONSTANTS}
                submitButtonFunction={submitEditUserGroupRequest}
                cancelButtonFunction={cancelEditUserGroupsPageFunction}
                isEditPage={true}
            />
        </ContentLayout>
    )
}
