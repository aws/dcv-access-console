// Policy 0: An admin can take any action on any resource
permit (
    principal,
    action,
    resource
) when {
    principal.role == Role::"Admin"
};

// Policy 1: Users can take any action that is listed under their role
permit (
    principal,
    action,
    resource
) when {
    action in principal.role.actions
};

// Policy 2: Anyone can view and connect to a session they own, they are a collaborator on,
// or they are in a group that is a collaborator
permit (
    principal,
    action in [
        Action::"viewSessionDetails",
        Action::"viewSessionScreenshotDetails",
        Action::"connectToSession"
    ],
    resource
) when {
    principal.loginUser == resource.owner || principal.loginUser in resource.collaborators
};

// Policy 3: Permit owners of sessions to edit and delete them, as long as they are not Guests
permit (
    principal,
    action in [
        Action::"editSession",
        Action::"deleteSession"
    ],
    resource
) when {
    principal.loginUser == resource.owner && principal.role != Role::"Guest"
};

// Policy 4: Permit Users to modify their own details. This is a special case, as the User role doesn't have the editUser permission.
permit (
    principal,
    action == Action::"editUser",
    resource
) when {
    principal == resource && principal.role == Role::"User"
};

// Policy 5: Permit Users to list and use SessionTemplates that have been shared with them
permit (
    principal,
    action in [
        Action::"viewSessionTemplateDetails",
        Action::"useSpecificSessionTemplate"
    ],
    resource
) when {
    principal == resource.owner ||
    principal in resource.publishedTo
};

// Policy 6: Permit Owners of SessionTemplates to publish/unpublish them, and create Sessions for others
permit (
    principal,
    action in [
        Action::"useSpecificSessionTemplate",
        Action::"useSessionTemplateForOther",
        Action::"publishSessionTemplate",
        Action::"unpublishSessionTemplate"
    ],
    resource
) when {
    principal == resource.owner
};

// Policy 7: Permit owners of Groups to add or remove Group members
permit (
    principal,
    action in [
        Action::"viewGroupDetails",
        Action::"addUserToGroup",
        Action::"removeUserFromGroup"
    ],
    resource
) when {
    principal == resource.owner
};

// Policy 8: Allow users to describe the details of Groups they are in
permit (
    principal,
    action == Action::"viewGroupDetails",
    resource
) when {
    principal in resource
};

// Policy 9: Forbid any principals to take any actions on any resources if they are disabled
forbid (
    principal,
    action,
    resource
) when {
    principal.disabled == true
};