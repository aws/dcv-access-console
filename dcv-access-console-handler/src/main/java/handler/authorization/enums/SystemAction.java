// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization.enums;

// Defines the actions a user can take on the system
public enum SystemAction {
    describeSessions,
    describeSessionsForOthers,
    getSessionScreenshots,
    describeServers,
    describeSessionTemplates,
    describeSessionTemplatesForOthers,
    createSessionTemplate,
    editSessionTemplate,
    validateSessionTemplate,
    publishSessionTemplate,
    unpublishSessionTemplate,
    createSessions,
    deleteSessions,
    deleteSessionTemplates,
    createSessionsForOthers,
    getSessionConnectionData,
    getSessionConnectionDataForOther,
    describeUsers,
    describeUserGroups,
    createUserGroup,
    deleteUserGroups,
    createUsers,
    importUsers,
    deleteUser,
    editUser,
    assignUserRole,
    describeUserInfo,
    addUserToGroup,
    removeUserFromGroup,
    describeGroups,
    describePermissions,
    createRole,
    deleteRole,
    editRole,
    describeUsersSharedWithSessionTemplate,
    describeUserGroupsSharedWithSessionTemplate
}
