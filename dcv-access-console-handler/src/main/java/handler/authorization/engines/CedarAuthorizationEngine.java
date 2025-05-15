// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization.engines;

import handler.authorization.engines.entities.SetShareListResponse;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.authorization.enums.SystemAction;
import handler.brokerclients.BrokerClient;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeSessionTemplatesResponse;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.DescribeSessionsUIResponse;
import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUserGroupsResponse;
import handler.model.DescribeUsersRequestData;
import handler.model.DescribeUsersResponse;
import handler.model.SessionTemplate;
import handler.model.SessionWithPermissions;
import handler.model.User;
import handler.model.UserGroup;
import handler.persistence.UserGroupUserMembership;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import handler.services.UserService;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.AuthorizationSuccessResponse;
import com.cedarpolicy.model.exception.AuthException;
import com.cedarpolicy.model.entity.Entity;
import com.cedarpolicy.model.policy.Policy;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.value.CedarList;
import com.cedarpolicy.value.EntityTypeName;
import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.PrimBool;
import com.cedarpolicy.value.PrimString;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CedarAuthorizationEngine extends AbstractAuthorizationEngine {
    // Confusing regex, but basically, the word 'permit' or 'forbid' followed by a space, then a left parenthesis then a
    // new line, then any collection of characters that doesn't contain a right parenthesis, then a right parenthesis,
    // then optionally a space and the word 'when' followed by a left curly bracket, then any characters that aren't
    // a right curly bracket, then a right curly bracket, and finally a semicolon.
    private static final String POLICY_MATCHER_REGEX = "(permit|forbid) \\(\\n\\s* [^)]*\\)( when \\{\\n\\s*[^}]*})?;";

    private static final String ACTIONS_ATTRIBUTE = "actions";
    private static final String ROLE_ATTRIBUTE = "role";
    private static final String DISABLED_ATTRIBUTE = "disabled";
    private static final String OWNER_ATTRIBUTE = "owner";
    private static final String DISPLAY_NAME_ATTRIBUTE = "displayName";
    private static final String LOGINUSER_ATTRIBUTE = "loginUser";

    @Value("${default-role}")
    private String defaultRole;
    private final UserService userService;
    private final UserGroupService userGroupService;
    private final SessionTemplateService sessionTemplateService;
    private final BrokerClient brokerClient;

    // TODO: Move these to configuration file
    private static final ShareLevel[] SESSION_SHARE_LEVELS = new ShareLevel[] {ShareLevel.collaborators};

    private static final ShareLevel[] SESSION_TEMPLATE_SHARE_LEVELS = new ShareLevel[] {ShareLevel.publishedTo};

    private final BasicAuthorizationEngine basicAuthorizationEngine;
    private final File policyFile;
    private final File roleFile;
    private static final EntityTypeName RoleTypeName = EntityTypeName.parse(ResourceType.Role.toString()).get();
    private static final EntityTypeName UserTypeName = EntityTypeName.parse(ResourceType.User.toString()).get();
    private static final EntityTypeName UserGroupTypeName = EntityTypeName.parse(ResourceType.Group.toString()).get();
    private static final EntityTypeName ActionTypeName = EntityTypeName.parse("Action").get();
    private static final EntityUID emptyResourceEUID = new EntityUID(EntityTypeName.parse("Resource").get(), "");
    private final PolicySet policies;
    private final HashMap<String, Entity> entitiesMap;
    private Set<Entity> entities;

    private final ObjectMapper mapper;
    private final ObjectWriter writer;

    public CedarAuthorizationEngine(@Value("${authorization-policies-location}") File policyFile,
                                    @Value("${authorization-roles-location}") File roleFile,
                                    @Value("${user-id-case-sensitive:true}")  boolean caseSensitive,
                                    BasicAuthorizationEngine basicAuthorizationEngine, ObjectMapper mapper, ObjectWriter writer,
                                    UserService userService,
                                    UserGroupService userGroupService,
                                    SessionTemplateService sessionTemplateService,
                                    BrokerClient brokerClient) {
        this.caseSensitive = caseSensitive;
        this.mapper = mapper;
        this.writer = writer;
        this.basicAuthorizationEngine = basicAuthorizationEngine;
        this.userService = userService;
        this.userGroupService = userGroupService;
        this.sessionTemplateService = sessionTemplateService;
        this.brokerClient = brokerClient;

        this.policyFile = policyFile;
        this.roleFile = roleFile;
        
        this.policies = new PolicySet();
        this.entitiesMap = new HashMap<>();

        loadEntities();
    }

    @Override
    public void loadEntities() {
        loadPoliciesFromFile(policyFile);
        entities = new HashSet<>();
        entitiesMap.clear();
        loadRolesFromFile(roleFile);
        loadUsersFromDb();
        loadUserGroupsFromDb();
        try {
            loadResourcesFromDb();
        } catch (Exception e) {
            log.error("Unable to load resources to the Authorization Engine", e);
        }
    }

    @Override
    public boolean isAuthorized(PrincipalType principalType, String principalUUID, SystemAction action)
            throws AuthorizationServiceException {
        EntityTypeName principalTypeName = EntityTypeName.parse(principalType.toString()).get();
        EntityUID cedarPrincipalEUID = new EntityUID(principalTypeName, normalizeUUID(principalUUID));
        EntityUID cedarActionEUID = new EntityUID(ActionTypeName, action.toString());
        // The resource doesn't matter, but the request will fail if it isn't present.
        AuthorizationRequest request = new AuthorizationRequest(cedarPrincipalEUID, cedarActionEUID, emptyResourceEUID,
                Collections.emptyMap());
        try {
            log.info("Checking authorization for principal: {} and action: {}", cedarPrincipalEUID, action);
            AuthorizationResponse response = this.basicAuthorizationEngine.isAuthorized(request, policies, entities);
            logAuthorization(response);
            return response.success.map(AuthorizationSuccessResponse::isAllowed).orElse(false);
        } catch (AuthException e) {
            log.error("Unable to reach authorization decision. ", e);
            String errorMsg = String.format("The Cedar Authorization Engine encountered an error and was unable to "
                    + "reach an authorization decision for principal: %s and action: %s", principalUUID, action);
            throw new AuthorizationServiceException(errorMsg);
        }
    }

    @Override
    public boolean isAuthorized(PrincipalType principalType, String principalUUID, ResourceAction action,
                                ResourceType resourceType, String resourceUUID) {
        EntityTypeName principalTypeName = EntityTypeName.parse(principalType.toString()).get();
        EntityTypeName resourceTypeName = EntityTypeName.parse(resourceType.toString()).get();
        EntityUID cedarPrincipalEUID = new EntityUID(principalTypeName, normalizeUUID(principalUUID));
        EntityUID cedarActionEUID = new EntityUID(ActionTypeName, action.toString());
        EntityUID cedarResourceEUID = new EntityUID(resourceTypeName, normalizeUUID(resourceType, resourceUUID));
        AuthorizationRequest request = new AuthorizationRequest(cedarPrincipalEUID, cedarActionEUID, cedarResourceEUID,
                Collections.emptyMap());
        try {
            log.info("Checking authorization for principal: {} and action: {} on resource {}", cedarPrincipalEUID,
                    action, cedarResourceEUID);
            AuthorizationResponse response = this.basicAuthorizationEngine.isAuthorized(request, policies, entities);
            logAuthorization(response);
            return response.success.map(AuthorizationSuccessResponse::isAllowed).orElse(false);
        } catch (AuthException e) {
            log.error("Unable to reach authorization decision. ", e);
            String errorMsg = String.format("The Cedar Authorization Engine encountered an error and was unable to "
                            + "reach an authorization decision for principal: %s, action: %s, and resource: %s", principalUUID,
                    action, resourceUUID);
            throw new AuthorizationServiceException(errorMsg);
        }
    }

    private void logAuthorization(AuthorizationResponse response) {
        log.info("Principal was{} permitted.", response.success.map(AuthorizationSuccessResponse::isAllowed).orElse(false) ? "" : " not");
        response.success.ifPresent(s -> log.info("Reasons for decision: {}", s));
        response.errors.ifPresent(e -> log.warn("Authorization engine denied access due to errors: {}", e.stream().map(er -> er.message).collect(Collectors.toList())));
    }

    public void addUser(String userUUID, String loginUsername, String displayName, String roleUUID, boolean isDisabled) {
        EntityUID userEUID = new EntityUID(UserTypeName, normalizeUUID(userUUID));
        Map<String, com.cedarpolicy.value.Value> attributes = new HashMap<>();
        if (StringUtils.isEmpty(loginUsername)) {
            loginUsername = normalizeUUID(userUUID);
        }
        attributes.put(LOGINUSER_ATTRIBUTE, new EntityUID(UserTypeName, loginUsername));
        attributes.put(ROLE_ATTRIBUTE, new EntityUID(RoleTypeName, roleUUID));
        attributes.put(DISPLAY_NAME_ATTRIBUTE, new PrimString(displayName));
        attributes.put(DISABLED_ATTRIBUTE, new PrimBool(isDisabled));

        if(entitiesMap.containsKey(userEUID.toString())) {
            Entity userEntity = entitiesMap.get(userEUID.toString());
            entities.remove(userEntity);
        }

        Entity userEntity = new Entity(userEUID, attributes, new HashSet<>());
        entitiesMap.put(userEUID.toString(), userEntity);
        entities.add(userEntity);
        log.info("Successfully added user {} to the Authorization Engine", userUUID);
    }

    @Override
    public boolean addUserWithPersistence(String userUUID) {
        String userId = normalizeUUID(userUUID);
        if(!userService.createUser(userId, userId, defaultRole)) {
            log.warn("Cannot add user {} as it already exists", userId);
            return false;
        }

        addUser(userId, userId, userId, defaultRole, false);
        return true;
    }

    @Override
    public boolean addGroup(String groupUUID) {
        EntityUID groupEUID = new EntityUID(UserGroupTypeName, normalizeUUID(groupUUID));
        if (entitiesMap.containsKey(groupEUID.toString())) {
            return false;
        }

        Entity groupEntity = new Entity(groupEUID, Collections.emptyMap(), Collections.emptySet());
        entitiesMap.put(groupEUID.toString(), groupEntity);
        entities.add(groupEntity);
        log.info("Successfully added group {} to the Authorization Engine", groupUUID);
        return true;
    }

    @Override
    public boolean addRole(String roleUUID, List<SystemAction> permissions) {
        // TODO: Save role to persistenceLayer
        EntityUID roleEUID = new EntityUID(RoleTypeName, roleUUID);
        if (entitiesMap.containsKey(roleEUID.toString())) {
            log.warn("Cannot add role {} as it already exists", roleUUID);
            return false;
        }
        CedarList cedarPermissions = new CedarList();
        for (SystemAction action : permissions) {
            cedarPermissions.add(new EntityUID(ActionTypeName, action.toString()));
        }
        Map<String, com.cedarpolicy.value.Value> attributes = new HashMap<>();
        attributes.put(ACTIONS_ATTRIBUTE, new CedarList(cedarPermissions));
        Entity roleEntity = new Entity(roleEUID, attributes, Collections.emptySet());
        entitiesMap.put(roleEUID.toString(), roleEntity);
        entities.add(roleEntity);
        return true;
    }

    @Override
    public boolean addSession(String sessionUUID, String ownerUUID) {
        return addResource(ResourceType.Session, sessionUUID, SESSION_SHARE_LEVELS, Optional.of(normalizeUUID(ownerUUID)));
    }

    @Override
    public boolean addServer(String resourceID) {
        throw new NotImplementedException();
    }

    @Override
    public boolean addSessionTemplate(String sessionTemplateId, String ownerUUID) {
        return addResource(ResourceType.SessionTemplate, sessionTemplateId, SESSION_TEMPLATE_SHARE_LEVELS,
                Optional.of(normalizeUUID(ownerUUID)));
    }

    private boolean addResource(ResourceType resourceType, String resourceUUID, ShareLevel[] shareLevels,
                                Optional<String> ownerUUID) {
        EntityTypeName resourceTypeName = EntityTypeName.parse(resourceType.toString()).get();
        EntityUID resourceEUID = new EntityUID(resourceTypeName, resourceUUID);
        Map<String, com.cedarpolicy.value.Value> attributes = new HashMap<>();
        for (ShareLevel shareLevel : shareLevels) {
            attributes.put(shareLevel.toString(), new CedarList());
        }
        ownerUUID.ifPresent(s -> attributes.put(OWNER_ATTRIBUTE, new EntityUID(UserTypeName, s)));
        if (entitiesMap.containsKey(resourceEUID.toString())) {
            log.warn("{} {} already exists on the Authorization Engine", resourceType, resourceUUID);
            return false;
        }
        Entity resourceEntity = new Entity(resourceEUID, attributes, Collections.emptySet());
        entitiesMap.put(resourceEUID.toString(), resourceEntity);
        entities.add(resourceEntity);
        log.info("Successfully added {} {} to the Authorization Engine", resourceType, resourceUUID);

        return true;
    }

    @Override
    public boolean setUserRole(String userUUID, String roleUUID) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public boolean addUserToGroup(String userUUID, String groupUUID) {
        String userEUID = new EntityUID(UserTypeName, normalizeUUID(userUUID)).toString();
        String groupEUID = new EntityUID(UserGroupTypeName, normalizeUUID(groupUUID)).toString();

        if (!entitiesMap.containsKey(userEUID)) {
            log.warn("Unable to add user {} to group {} because the user could " +
                    "not be found in the authorization engine", userUUID, groupUUID);
            return false;
        }

        if (!entitiesMap.containsKey(groupEUID)) {
            log.warn("Unable to add user {} to group {} because the group could " +
                    "not be found in the authorization engine", userUUID, groupUUID);
            return false;
        }

        entitiesMap.get(userEUID).parentsEUIDs.add(entitiesMap.get(groupEUID).getEUID());
        log.info("Successfully added user {} to group {}", userUUID, groupUUID);
        return true;
    }

    @Override
    public boolean removeUserFromGroup(String userUUID, String groupUUID) {
        String userEUID = new EntityUID(UserTypeName, normalizeUUID(userUUID)).toString();
        String groupEUID = new EntityUID(UserGroupTypeName, normalizeUUID(groupUUID)).toString();

        if (!entitiesMap.containsKey(userEUID)) {
            log.warn("Unable to remove user {} from group {} because the user could " +
                    "not be found in the authorization engine", userUUID, groupUUID);
            return false;
        }

        if (!entitiesMap.containsKey(groupEUID)) {
            log.warn("Unable to from user {} from group {} because the group could " +
                    "not be found in the authorization engine", userUUID, groupUUID);
            return false;
        }

        entitiesMap.get(userEUID).parentsEUIDs.remove(entitiesMap.get(groupEUID).getEUID());
        log.info("Successfully removed user {} from group {}", userUUID, groupUUID);
        return true;
    }

    @Override
    public boolean addPermissionToRole(String roleUUID, SystemAction permission) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public boolean deleteResource(ResourceType resourceType, String resourceId) {
        EntityTypeName resourceTypeName = EntityTypeName.parse(resourceType.toString()).get();
        String resourceUUID = normalizeUUID(resourceType, resourceId);
        String resourceEUID = new EntityUID(resourceTypeName, resourceUUID).toString();
        if (!entitiesMap.containsKey(resourceEUID)) {
            log.warn("Unable to find {} {} on the Authorization Engine", resourceType, resourceId);
            return false;
        }

        Entity resourceEntity = entitiesMap.get(resourceEUID);
        entitiesMap.remove(resourceEUID);
        entities.remove(resourceEntity);
        log.info("Successfully deleted {} {} from the Authorization Engine", resourceType, resourceId);
        return true;
    }

    @Override
    public boolean removePermissionFromRole(String roleUUID, SystemAction permission) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public boolean removeRole(String roleUUID) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public boolean removeGroup(String groupUUID) {
        String groupEUID = new EntityUID(UserGroupTypeName, groupUUID).toString();
        if (!entitiesMap.containsKey(groupEUID)) {
            log.warn("Unable to find group {} on the Authorization Engine", groupUUID);
            return false;
        }
        Entity userGroupEntity = entitiesMap.get(groupEUID);
        entitiesMap.remove(groupEUID);
        entities.remove(userGroupEntity);
        log.info("Successfully deleted group {} from the Authorization Engine", groupUUID);

        return true;
    }

    @Override
    public boolean removeUser(String userUUID) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public boolean addPrincipalToSharedList(PrincipalType principalType, String principalUUID,
                                            ResourceType resourceType, String resourceUUID, ShareLevel shareLevel) {
        EntityTypeName principalTypeName = EntityTypeName.parse(principalType.toString()).get();
        EntityTypeName resourceTypeName = EntityTypeName.parse(resourceType.toString()).get();
        String cedarPrincipalEUID = new EntityUID(principalTypeName, normalizeUUID(principalUUID)).toString();
        String cedarResourceEUID = new EntityUID(resourceTypeName, normalizeUUID(resourceType, resourceUUID)).toString();
        log.info("Adding {} {} to shareList {} on {} {}", principalType, normalizeUUID(principalUUID), shareLevel, resourceType,
                normalizeUUID(resourceType, resourceUUID));
        if (!entitiesMap.containsKey(cedarResourceEUID)) {
            log.warn("Unable to find {} {}", resourceType, resourceUUID);
            return false;
        }
        if (!entitiesMap.containsKey(cedarPrincipalEUID) && !entitiesMap.containsKey(cedarPrincipalEUID)) {
            log.warn("Unable to find {} {}", principalType, principalUUID);
            return false;
        }
        Entity resourceEntity = entitiesMap.get(cedarResourceEUID);
        if (resourceEntity.attrs.containsKey(shareLevel.toString()) && resourceEntity.attrs.get(
                shareLevel.toString()) instanceof CedarList sharedList) {
            EntityUID sharedWith = new EntityUID(principalTypeName, cedarPrincipalEUID);
            if (!sharedList.contains(sharedWith)) {
                log.info("Successfully added {} {} to shareList {} on {} {}", principalType, principalUUID, shareLevel,
                        resourceType, resourceUUID);
                sharedList.add(sharedWith);
            } else {
                log.warn("{} {} is already shared with {} {}", principalType, principalUUID, resourceType, resourceUUID);
                return false;
            }
        } else {
            log.warn("Share List with name {} does not exist on {} {}", shareLevel, resourceType, resourceUUID);
            return false;
        }
        if (resourceType == ResourceType.SessionTemplate) {
            log.info("Updating SessionTemplate {} in persistence layer", resourceUUID);
            if (PrincipalType.User.equals(principalType)) {
                sessionTemplateService.publishSessionTemplate(resourceUUID, List.of(principalUUID), Collections.emptyList());
            } else {
                sessionTemplateService.publishSessionTemplate(resourceUUID, Collections.emptyList(), List.of(principalUUID));
            }
        }
        return true;
    }

    @Override
    public SetShareListResponse setShareList(List<String> usersList, List<String> groupsList, ResourceType resourceType,
            String resourceUUID, ShareLevel shareLevel) {
        SetShareListResponse response;

        Set<EntityUID> cedarEUIDUsersList = usersList.stream()
                .map(userUUID -> new EntityUID(UserTypeName, normalizeUUID(userUUID)))
                .collect(Collectors.toSet());
        Set<EntityUID> cedarEUIDGroupsList = groupsList.stream()
                .map(groupUUID -> new EntityUID(UserGroupTypeName, normalizeUUID(groupUUID)))
                .collect(Collectors.toSet());
        CedarList entitiesToAddToShareList = new CedarList();

        List<String> presentUsers = new ArrayList<>();
        List<String> presentGroups = new ArrayList<>();

        for (EntityUID user : cedarEUIDUsersList) {
            if (entitiesMap.containsKey(user.toString())) {
                entitiesToAddToShareList.add(user);
                presentUsers.add(user.getId().toString());
            } else {
                log.warn("Unable to find user {} on the Authorization Engine", user.getId());
            }
        }

        for (EntityUID group : cedarEUIDGroupsList) {
            if (entitiesMap.containsKey(group.toString())) {
                entitiesToAddToShareList.add(group);
                presentGroups.add(group.getId().toString());
            } else {
                log.warn("Unable to find group {} on the Authorization Engine", group.getId());
            }
        }

        setResourceShareList(resourceType, resourceUUID, shareLevel, entitiesToAddToShareList);

        if (ResourceType.SessionTemplate.equals(resourceType)) {
            try {
                response = sessionTemplateService.setSessionTemplateShareList(resourceUUID, presentUsers, presentGroups);
            } catch (Exception e) {
                String msg = String.format("Unable to update SessionTemplate %s on the persistence layer", resourceUUID);
                throw new RuntimeException(msg, e);
            }
            log.info("Updated SessionTemplate {} on the persistence layer", resourceUUID);
        } else {
            String msg = String.format("Sharing is not supported for %s", resourceType);
            throw new UnsupportedOperationException(msg);
        }

        List<String> unsuccessfulUsers = usersList.stream().filter(userId -> !presentUsers.contains(userId)).toList();
        if(!unsuccessfulUsers.isEmpty()) {
            response.getUnSuccessfulUsers().addAll(unsuccessfulUsers);
        }

        List<String> unsuccessfulGroups = groupsList.stream().filter(groupId -> !presentGroups.contains(groupId)).toList();
        if(!unsuccessfulGroups.isEmpty()) {
            response.getUnSuccessfulGroups().addAll(unsuccessfulGroups);
        }

        log.info("Successfully set shareList {} on {} {}", shareLevel, resourceType, resourceUUID);
        return response;
    }

    // This method could use some work. It should check that each entity is present in Cedar, and return some sort
    // of response to indicate which entities were successful and which weren't
    private void setResourceShareList(ResourceType resourceType, String resourceUUID, ShareLevel shareLevel, CedarList entityList) {
        EntityTypeName resourceTypeName = EntityTypeName.parse(resourceType.toString()).get();
        String cedarResourceEUID = new EntityUID(resourceTypeName, resourceUUID).toString();

        if (!entitiesMap.containsKey(cedarResourceEUID)) {
            String msg = String.format("Unable to find resource %s on the Authorization Engine", cedarResourceEUID);
            throw new MissingResourceException(msg, resourceType.toString(), resourceUUID);
        }

        Entity resourceEntity = entitiesMap.get(cedarResourceEUID);
        if (!resourceEntity.attrs.containsKey(shareLevel.toString()) || !(resourceEntity.attrs.get(
                shareLevel.toString()) instanceof CedarList)) {
            String msg = String.format("Share List with name %s does not exist on this resource", shareLevel);
            throw new UnsupportedOperationException(msg);
        }

        //The share list for the resource entity is entirely replaced
        resourceEntity.attrs.put(shareLevel.toString(), entityList);
    }

    @Override
    public boolean removePrincipalFromSharedList(PrincipalType principalType, String principalUUID,
                                                 ResourceType resourceType, String resourceUUID, ShareLevel shareLevel) {
        EntityTypeName principalTypeName = EntityTypeName.parse(principalType.toString()).get();
        EntityTypeName resourceTypeName = EntityTypeName.parse(resourceType.toString()).get();
        String cedarPrincipalEUID = new EntityUID(principalTypeName, normalizeUUID(principalUUID)).toString();
        String cedarResourceEUID = new EntityUID(resourceTypeName, normalizeUUID(resourceType, resourceUUID)).toString();
        log.info("Removing {} from shareList {} on {}", cedarPrincipalEUID, shareLevel, cedarResourceEUID);
        if (!entitiesMap.containsKey(cedarResourceEUID)) {
            log.warn("Unable to find resource {}", cedarResourceEUID);
            return false;
        }
        if (!entitiesMap.containsKey(cedarPrincipalEUID) && !entitiesMap.containsKey(cedarPrincipalEUID)) {
            log.warn("Unable to find {}", cedarPrincipalEUID);
            return false;
        }
        Entity resourceEntity = entitiesMap.get(cedarResourceEUID);
        if (resourceEntity.attrs.containsKey(shareLevel.toString()) && resourceEntity.attrs.get(
                shareLevel.toString()) instanceof CedarList sharedList) {
            EntityUID sharedWith = new EntityUID(principalTypeName, cedarPrincipalEUID);
            if (!sharedList.contains(sharedWith)) {
                log.info("Successfully removed {} {} from shareList {} on {} {}", principalType, principalUUID, shareLevel,
                        resourceType, resourceUUID);
                sharedList.remove(sharedWith);
            } else {
                log.warn("Principal {} is not present on the share list", principalUUID);
                return false;
            }
        } else {
            log.warn("Share List with name {} does not exist on this resource", shareLevel);
            return false;
        }
        return true;
    }

    @Override
    public List<String> getSharedListForResource(ResourceType resourceType, String resourceUUID, ShareLevel shareLevel,
            PrincipalType principalType) {
        EntityTypeName resourceTypeName = EntityTypeName.parse(resourceType.toString()).get();
        EntityTypeName principalTypeName = EntityTypeName.parse(principalType.toString()).get();
        String cedarResourceEUID = new EntityUID(resourceTypeName, normalizeUUID(resourceType, resourceUUID)).toString();
        if (!entitiesMap.containsKey(cedarResourceEUID)) {
            log.warn("Unable to find resource {}", cedarResourceEUID);
            return Collections.emptyList();
        }

        Entity resourceEntity = entitiesMap.get(cedarResourceEUID);
        if (resourceEntity.attrs.containsKey(shareLevel.toString()) && resourceEntity.attrs.get(
                shareLevel.toString()) instanceof CedarList shareList) {
            List<String> principalsSharedTo = new ArrayList<>();
            shareList.listIterator().forEachRemaining(principal -> {
                if (principal instanceof EntityUID entityUID) {
                    EntityTypeName typeName = entityUID.getType();
                    String id = entityUID.getId().toString();
                    if (typeName.equals(principalTypeName)) {
                        principalsSharedTo.add(normalizeUUID(id));
                    }
                } else {
                    log.warn("Invalid principal {} with type: {}", principal, principal.getClass().getName());
                }
            });
            return principalsSharedTo;
        }
        log.warn("Share List with name {} does not exist on this resource", shareLevel);
        return Collections.emptyList();
    }

    @Override
    public String getUserRole(String userUUID) {
        Entity userEntity = getUserEntity(userUUID);
        return ((EntityUID) userEntity.attrs.get(ROLE_ATTRIBUTE)).getId().toString();
    }

    @Override
    public String getUserDisplayName(String userUUID) {
        Entity userEntity = getUserEntity(userUUID);
        return userEntity.attrs.get(DISPLAY_NAME_ATTRIBUTE).toString();
    }

    private Entity getUserEntity(String userUUID) {
        String cedarUserEUID = new EntityUID(UserTypeName, normalizeUUID(userUUID)).toString();
        if (!entitiesMap.containsKey(cedarUserEUID)) {
            throw new UsernameNotFoundException("Unable to find user " + userUUID);
        }
        return entitiesMap.get(cedarUserEUID);
    }

    private void loadPoliciesFromFile(File policyFile) {
        try {
            String contents = new String(Files.readAllBytes(policyFile.toPath()));
            // Pull out policies from the contest by matching with regex
            Matcher m = Pattern.compile(POLICY_MATCHER_REGEX).matcher(contents);
            Set<Policy> policySet = new HashSet<>();
            int i = 0;
            while (m.find()) {
                Policy policy = new Policy(m.group(), "Policy " + i++);
                policySet.add(policy);
            }
            this.policies.policies = policySet;
        } catch (IOException e) {
            log.error("Failed to load policies from file: {}", policyFile.getPath(), e);
            throw new RuntimeException(e);
        }
    }

    private void loadRolesFromFile(File roleFile) {
        try {
            List<Role> rolesList = mapper.readValue(roleFile, new TypeReference<>() {
            });
            for (Role role : rolesList) {
                ArrayList<SystemAction> permissions = new ArrayList<>();
                for (String permission : role.permissions) {
                    try {
                        SystemAction action = SystemAction.valueOf(permission);
                        permissions.add(action);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid action: {} in role file {}", permission, roleFile);
                    }
                }
                this.addRole(role.roleName, permissions);
            }
        } catch (IOException e) {
            log.error("ERROR: Failed to read role file {}, roles not loaded. Error: ", roleFile, e);
            throw new RuntimeException(e);
        }
    }

    private void loadUsersFromDb() {
        DescribeUsersResponse response;
        String token = null;
        do {
            response = userService.describeUsers(new DescribeUsersRequestData().nextToken(token));
            for (User user : response.getUsers()) {
                addUser(user.getUserId(), user.getLoginUsername(), user.getDisplayName(), user.getRole(), user.getIsDisabled());
            }
            token = response.getNextToken();
        } while (token != null);
    }

    private void loadUserGroupsFromDb() {
        DescribeUserGroupsResponse describeUserGroupsResponse;
        String token = null;
        do {
            describeUserGroupsResponse = userGroupService.describeUserGroups(new DescribeUserGroupsRequestData().nextToken(token));
            for (UserGroup group : describeUserGroupsResponse.getUserGroups()) {
                EntityUID groupEUID = new EntityUID(UserGroupTypeName, normalizeUUID(group.getUserGroupId()));

                if (entitiesMap.containsKey(groupEUID.toString())) {
                    log.info("Found existing group with EUID: {}", groupEUID);
                } else {
                    log.info("Creating new group with EUID: {}", groupEUID);
                    Entity groupEntity = new Entity(groupEUID, Collections.emptyMap(), Collections.emptySet());
                    entitiesMap.put(groupEUID.toString(), groupEntity);
                    entities.add(groupEntity);
                }
            }
            token = describeUserGroupsResponse.getNextToken();
        } while (token != null);

        // This iterable could be quite large. Worth finding a solution to shrink it
        Iterable<UserGroupUserMembership> memberships = userGroupService.getUserGroupUserMemberships();
        for (UserGroupUserMembership membership : memberships) {
            String userEUID = new EntityUID(UserTypeName, membership.getId().getUserId()).toString();
            Entity userEntity = entitiesMap.get(userEUID);
            String groupEUID = new EntityUID(UserGroupTypeName, membership.getId().getUserGroupId()).toString();
            Entity groupEntity = entitiesMap.get(groupEUID);

            userEntity.parentsEUIDs.add(groupEntity.getEUID());
        }
    }

    private void loadResourcesFromDb() throws Exception {
        DescribeSessionTemplatesResponse describeSessionTemplatesResponse;
        String token = null;
        do {
            describeSessionTemplatesResponse = sessionTemplateService.describeSessionTemplates(new DescribeSessionTemplatesRequestData().nextToken(token));
            for (SessionTemplate sessionTemplate: describeSessionTemplatesResponse.getSessionTemplates()) {
                addSessionTemplate(sessionTemplate.getId(), sessionTemplate.getCreatedBy());
                List<String> userIds = sessionTemplateService.getUserIdsBySessionTemplate(sessionTemplate.getId());
                List<String> groupIds = sessionTemplateService.getUserGroupIdsBySessionTemplate(sessionTemplate.getId());

                CedarList entitiesToPublish = new CedarList();
                for (String userID : userIds) {
                    EntityUID userEUID = new EntityUID(UserTypeName, userID);
                    Entity userEntity = entitiesMap.get(userEUID.toString());
                    if (userEntity != null) {
                        entitiesToPublish.add(userEntity.getEUID());
                        log.debug("Added user {} to share list for Session Template {}", userID, sessionTemplate.getId());
                    } else {
                        log.warn("Unable to add user {} to share list for Session Template {}" +
                                " as they don't exist in the authorization engine", userID, sessionTemplate.getId());
                    }
                }
                for (String groupID : groupIds) {
                    EntityUID groupEUID = new EntityUID(UserGroupTypeName, groupID);
                    Entity groupEntity = entitiesMap.get(groupEUID.toString());
                    if (groupEntity != null) {
                        entitiesToPublish.add(groupEntity.getEUID());
                        log.debug("Added group {} to share list for Session Template {}", groupID, sessionTemplate.getId());
                    } else {
                        log.warn("Unable to add group {} to share list for Session Template {}" +
                                " as they don't exist in the authorization engine", groupID, sessionTemplate.getId());
                    }
                }
                setResourceShareList(ResourceType.SessionTemplate, sessionTemplate.getId(), ShareLevel.publishedTo, entitiesToPublish);
            }
            token = describeSessionTemplatesResponse.getNextToken();
        } while(token != null);
        log.info("Loaded all Session Templates from the persistence layer onto the Authorization Engine");

        DescribeSessionsUIResponse describeSessionsResponse;
        do {
            describeSessionsResponse = brokerClient.describeSessions(new DescribeSessionsUIRequestData());
            for (SessionWithPermissions session: describeSessionsResponse.getSessions()) {
                addSession(session.getId(), session.getOwner());
            }
        } while(describeSessionsResponse.getNextToken() != null);
        log.info("Loaded all Sessions from the broker onto the Authorization Engine");
    }

    private String normalizeUUID(String UUID) {
        if(!caseSensitive && UUID != null) {
            return UUID.toLowerCase();
        }
        return UUID;
    }
    private String normalizeUUID(ResourceType type, String UUID) {
        if((ResourceType.User.equals(type) || ResourceType.Group.equals(type))) {
            return normalizeUUID(UUID);
        }
        return UUID;
    }

    public List<String> getRoles() {
        return entitiesMap.keySet().stream().filter(r -> ResourceType.Role.toString().equals(EntityUID.parse(r).get().getType().getBaseName())).map(r -> EntityUID.parse(r).get().getId().toString()).collect(Collectors.toList());
    }

    @Override
    public String getDefaultUserRole() {
        return defaultRole;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Role {
        String roleName;
        List<String> permissions;
    }
}