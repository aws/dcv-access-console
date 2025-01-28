// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization.enums;

public enum ResourceAction {
    viewServerDetails,
    openHost,
    closeHost,
    viewUserDetails,
    deleteUser,
    editUser,
    editUserGroup,
    addUserToGroup,
    removeUserFromGroup,
    viewGroupDetails,
    viewSessionDetails,
    viewSessionScreenshotDetails,
    deleteSession,
    editSession,
    connectToSession,
    viewSessionTemplateDetails,
    deleteSessionTemplate,
    deleteUserGroup,
    useSpecificSessionTemplate,
    editSpecificSessionTemplate,
    publishSpecificSessionTemplate,
    unpublishSpecificSessionTemplate,
    useSessionTemplateForOther,
    viewUsersSharedWithSessionTemplate,
    viewUserGroupsSharedWithSessionTemplate
}
