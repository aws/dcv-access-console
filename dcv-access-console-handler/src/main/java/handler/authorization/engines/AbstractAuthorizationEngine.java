// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization.engines;

import handler.authorization.engines.entities.SetShareListResponse;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.authorization.enums.SystemAction;
import java.util.List;
import org.springframework.security.access.AuthorizationServiceException;

public abstract class AbstractAuthorizationEngine {

    protected boolean caseSensitive;

    /**
     * Loads the policies, roles, users, groups, and resources from the persistence layer.
     */
    public abstract void loadEntities();

    /**
     * Checks if the principal is authorized to take that action.
     * @param principalType The type of principal, as defined by the PrincipalType enum.
     * @param principalUUID The unique identifier of the principal.
     * @param action The action the principal is taking.
     * @return Returns true if the principal is authorized
     * @throws AuthorizationServiceException if the Authorization Engine is not able to determine if the principal
     * is authorized or not.
     */
    public abstract boolean isAuthorized(PrincipalType principalType, String principalUUID,
                                         SystemAction action) throws AuthorizationServiceException;

    /**
     * Checks if the principal is authorized to take that action on that resource.
     * @param principalType The type of principal, as defined by the PrincipalType enum.
     * @param principalUUID The unique identifier of the principal.
     * @param action The action the principal is taking.
     * @param resourceType The type of resource, as defined by the ResourceType enum.
     * @param resourceUUID The unique identifier of the resource the principal is taking that action on.
     * @return Returns true if the principal is authorized.
     * @throws AuthorizationServiceException if the Authorization Engine is not able to determine if the principal is
     * authorized or not.
     */
    public abstract boolean isAuthorized(PrincipalType principalType, String principalUUID, ResourceAction action,
                                         ResourceType resourceType, String resourceUUID) throws AuthorizationServiceException;

    /**
     * Adds a new user to the system with the default role and saves it to the persistence layer.
     * @param userUUID The unique identifier of the user to add.
     * @return Returns true if the user was added successfully, false if not.
     */
    public abstract boolean addUserWithPersistence(String userUUID);

    /**
     * Adds a new user to the system with the role specified.
     * @param userUUID The unique identifier of the user to add.
     * @param loginUsername The Session Manager login username.
     * @param displayName The display name of the user. If not present, defaults to the userUUID.
     * @param roleUUID The unique identifier of the role to assign to the user.
     * @param isDisabled The disabled state of the user.
     */
    public abstract void addUser(String userUUID, String loginUsername, String displayName, String roleUUID, boolean isDisabled);

    /**
     * Adds a new group to the system with the UUID specified.
     * @param groupUUID The unique identifier of the group to be added.
     * @return Returns true if the group was successfully added, false if not.
     */
    public abstract boolean addGroup(String groupUUID);

    /**
     * Adds the role to the system with the UUID specified and list of permissions.
     * @param roleUUID The unique identifier of the new role to be added.
     * @param permissions A list of actions that the new role will have permission to take.
     * @return Return true if the role was successfully added, false if not.
     */
    public abstract boolean addRole(String roleUUID, List<SystemAction> permissions);

    /**
     * Add the session to the system with the specified owner. Returns true if the session was successfully added.
     * @param sessionUUID The unique identifier of the session to be added.
     * @param ownerUUID The unique identifier of the user to be assigned as the owner of the session.
     * @return Return true if the session was successfully added, false if not.
     */
    public abstract boolean addSession(String sessionUUID, String ownerUUID);

    /**
     * Add the server to the system with the specified UUID.
     * @param serverUUID The unique identifiter of the server to be added to the system
     * @return Return true if the server was successfully added, false if not.
     */
    public abstract boolean addServer(String serverUUID);

    /**
     * Add the session template to the system.
     * @param sessionUUID The unique identifier of the Session Template to be added to the system.
     * @return Return true if the Session Template was successfully added to the system, false if not.
     */
    public abstract boolean addSessionTemplate(String sessionUUID, String ownerUUID);

    /**
     * Sets the user's role to the roleUUID specified.
     * @param userUUID The unique identifier of the user whose role will be changed.
     * @param roleUUID The unique identifier of the role to assign to the user.
     * @return Return true if the user's role was successfully changed, false if not.
     */
    public abstract boolean setUserRole(String userUUID, String roleUUID);

    /**
     * Adds the user to the groups with specified groupUUIDs.
     * @param userUUID The unique identifier of the user who will be added to the group.
     * @param groupUUID The groups to add the user to.
     * @return Return true if the user was successfully added, false if not.
     */
    public abstract boolean addUserToGroup(String userUUID, String groupUUID);

    /**
     * Removes the user from the specified group
     * @param userUUID The unique identifier of the users who will be removed from the groups
     * @param groupUUID The group to remove the user from
     * @return Return true if the user was successfully removed, false if not
     */
    public abstract boolean removeUserFromGroup(String userUUID, String groupUUID);

    /**
     * Adds the permission to the role specified.
     * @param roleUUID The unique identifier of the role to add the permission to.
     * @param permission The unique identifier of the permission to be added to the role.
     * @return Return true if the permission was successfully added to the role, false if not.
     */
    public abstract boolean addPermissionToRole(String roleUUID, SystemAction permission);

    /**
     * Removes the Resource from the AuthorizationEngine
     * @param resourceType The type of resource to be deleted, defined by the ResourceType enum
     * @param sessionUUID The unique identifier of the resource to be deleted
     * @return Returns true if the resource was successfully deleted
     */
    public abstract boolean deleteResource(ResourceType resourceType, String sessionUUID);

    /**
     * Removes the permission from the role specified.
     * @param roleUUID The unique identifier of the role to remove the permission from.
     * @param permission The unique identifier of the permission to remove from the role.
     * @return Returns true if the permission was successfully removed, false if not.
     */
    public abstract boolean removePermissionFromRole(String roleUUID, SystemAction permission);

    /**
     * Removes the role from the system with the UUID specified.
     * @param roleUUID The unique identifier of the role to remove from the system.
     * @return Returns true if the role was successfully removed, false if not.
     */
    public abstract boolean removeRole(String roleUUID);

    /**
     * Removes the group from the system with the UUID specified.  This does not remove the relationship between
     * users and groups. To do that, use removeUserFromGroup.
     * @param groupUUID The unique identifier of the group to remove from the system.
     * @return Returns true if the group was successfully removed, false if not.
     */
    public abstract boolean removeGroup(String groupUUID);

    /**
     * Removes the user from the system with the UUID specified.
     * @param userUUID The unique identifier of the user to remove from the system.
     * @return Returns true if the user was successfully removed, false if not.
     */
    public abstract boolean removeUser(String userUUID);

    /**
     * Add the principal to the list of principals that the resource is shared with.
     * @param principalType A String representing the type of principal. Either 'Group' or 'User'.
     * @param principalUUID The unique identifier of the principal to add to the share list.
     * @param resourceType The type of resource, as defined by the ResourceType enum.
     * @param resourceUUID The unique identifier of the resource to change the share list of.
     * @param shareLevel The share list to add the principal to, e.g. 'collaborators'. Defined by the ShareLevel enum
     * @return Return true if the principal was successfully added to the share list of the resource, false if not.
     */
    public abstract boolean addPrincipalToSharedList(PrincipalType principalType, String principalUUID, ResourceType resourceType,
                                                     String resourceUUID, ShareLevel shareLevel);


    /**
     * Sets the specified resource's share list to be the list of users and the list of groups.
     * @param usersList A list of User's unique identifiers to set the resource as shared with.
     * @param groupsList A list of Group's unique identifiers to set the resource as shared with.
     * @param resourceType The type of resource, as defined by the ResourceType enum.
     * @param resourceUUID The unique identifier of the resource to change the share list of.
     * @param shareLevel The share list to add the principal to, e.g. 'collaborators'. Defined by the ShareLevel enum
     * @return Return SetShareListResponse containing successful and unsuccessful user and groups.
     */
    public abstract SetShareListResponse setShareList(List<String> usersList, List<String> groupsList, ResourceType resourceType,
                                                      String resourceUUID, ShareLevel shareLevel);

    /**
     * Removes the principal from the list of principals that the resource is shared with.
     * @param principalType A String representing the type of principal. Either 'Group' or 'User'.
     * @param principalUUID The unique identifier of the principal to remove from the share list.
     * @param resourceType The type of resource, as defined by the ResourceType enum.
     * @param resourceUUID The unique identifier of the resource to change the share list of.
     * @param shareLevel The share list to add the principal to, e.g. 'collaborators'. Defined by the ShareLevel enum
     * @return Return true if the principal was successfully removed from the share list of the resource, false if not.
     */
    public abstract boolean removePrincipalFromSharedList(PrincipalType principalType, String principalUUID, ResourceType resourceType,
                                                          String resourceUUID, ShareLevel shareLevel);

    /**
     * Provides the role of the user.
     * @param userUUID The unique identifier of the user
     * @return Returns a String of the role of the user
     */
    public abstract String getUserRole(String userUUID);

    /**
     * Provides the default role of a user.
     * @return Returns a String of the default role of a user
     */
    public abstract String getDefaultUserRole();

    /**
     * Provides the display name of the user.
     * @param userUUID The unique identifier of the user
     * @return Returns a String of the display name of the user
     */
    public abstract String getUserDisplayName(String userUUID);

    /**
     * Provides principals the resource is shared to
     * @param resourceType A String representing the type of resource, e.g. 'Session'.
     * @param resourceUUID The unique identifier of the resource to change the share list of.
     * @param shareLevel The unique identifier of the share list to add the principal to, e.g. 'collaborators'.
     * @param principalType A String representing the type of principal. Either 'Group' or 'User'.
     * @return Return list of the principals in the share list, empty array if not.
     */
    public abstract List<String> getSharedListForResource(ResourceType resourceType, String resourceUUID, ShareLevel shareLevel, PrincipalType principalType);

    /**
     * @return Returns the list of roles in the authorization engine
     */
    public abstract List<String> getRoles();
}
