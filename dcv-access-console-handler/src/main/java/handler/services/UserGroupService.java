// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.services;

import handler.exceptions.BadRequestException;
import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUserGroupsResponse;
import handler.model.DescribeUsersRequestData;
import handler.model.EditUserGroupRequestData;
import handler.model.FilterToken;
import handler.model.SessionTemplate;
import handler.model.User;
import handler.model.UserGroup;
import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.UserEntity;
import handler.persistence.UserGroupEntity;
import handler.persistence.UserGroupUser;
import handler.persistence.UserGroupUserMembership;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.SessionTemplatePublishedToUserGroupRepository;
import handler.repositories.UserGroupUserMembershipRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.Filter;
import handler.utils.NextToken;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final PagingAndSortingCrudRepository<UserGroupEntity, String> userGroupRepository;
    private final SessionTemplateService sessionTemplateService;
    private final UserGroupUserMembershipRepository userGroupUserMembershipRepository;
    private final Filter<DescribeUserGroupsRequestData, UserGroupEntity> userGroupFilter;


    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    public UserGroup createUserGroup(String groupId, boolean isImported) {
        return this.createUserGroup(groupId, groupId, isImported);
    }

    public UserGroup createUserGroup(String groupId, @Nullable String displayName, boolean isImported) {
        if (userGroupRepository.existsById(groupId)) { // Could optimize here by storing existing group IDs in a cache
            return null;
        }
        UserGroupEntity group = createNewUserGroupEntity(groupId, displayName, isImported);
        return userGroupRepository.save(group);
    }

    public UserGroup editUserGroup(EditUserGroupRequestData request) {
        Optional<UserGroupEntity> userGroup = userGroupRepository.findById(request.getUserGroupId());
        if (userGroup.isEmpty()) {
            log.warn("Unable to find userGroup {} in DB", request.getUserGroupId());
            throw new BadRequestException("Unable to find userGroup");
        }

        // Update the display name and the modified time.
        UserGroupEntity editedUserGroup = updateExistingUserGroupEntity(
                userGroup.get(), request.getDisplayName(), userGroup.get().getIsImported()
        );
        userGroupRepository.save(editedUserGroup);

        // Remove the requested users from the Group
        if (request.getUserIdsToRemove() != null && !request.getUserIdsToRemove().isEmpty()) {
            List<UserGroupUser> userGroupUsersToRemove = new ArrayList<>();
            for (String userId : request.getUserIdsToRemove()) {
                if (userId == null) {
                    continue;
                }

                UserGroupUser userGroupUser = new UserGroupUser();
                userGroupUser.setUserGroupId(editedUserGroup.getUserGroupId());
                userGroupUser.setUserId(userId);

                userGroupUsersToRemove.add(userGroupUser);
            }
            log.info("Removing {} users from group {}", userGroupUsersToRemove.size(), editedUserGroup.getUserGroupId());
            removeMembersFromGroup(userGroupUsersToRemove);
        }

        // Add the requested users to the Group
        if (request.getUserIdsToAdd() != null && !request.getUserIdsToAdd().isEmpty()) {
            List<UserGroupUserMembership> userGroupUsersToAdd = new ArrayList<>();
            for (String userId : request.getUserIdsToAdd()) {
                if (userId == null) {
                    continue;
                }

                log.info("Saving membership for user: {}", userId);
                UserGroupUserMembership membership = new UserGroupUserMembership(
                        editedUserGroup,
                        (UserEntity) new UserEntity().userId(userId)
                );

                userGroupUsersToAdd.add(membership);
            }
            log.info("Adding {} users to group {}", userGroupUsersToAdd.size(), editedUserGroup.getUserGroupId());
            addMembersToGroup(userGroupUsersToAdd);
        }

        // Unpublish the requested session template from the Group
        if (request.getSessionTemplateIdsToRemove() != null && !request.getSessionTemplateIdsToRemove().isEmpty()) {
            List<SessionTemplatePublishedToUserGroup> sessionTemplateToUnpublish = new ArrayList<>();
            for (String sessionTemplateId : request.getSessionTemplateIdsToRemove()) {
                if (sessionTemplateId == null) {
                    continue;
                }
                SessionTemplatePublishedToUserGroup sessionTemplate = new SessionTemplatePublishedToUserGroup(
                        new SessionTemplate().id(sessionTemplateId),
                        editedUserGroup
                );

                sessionTemplateToUnpublish.add(sessionTemplate);
            }
            log.info("Unpublishing {} session templates from group {}", sessionTemplateToUnpublish.size(), editedUserGroup.getUserGroupId());
            sessionTemplateService.removeUserGroupsFromSessionTemplate(sessionTemplateToUnpublish);
        }

        // Publish the requested session template to the Group
        if (request.getSessionTemplateIdsToAdd() != null && !request.getSessionTemplateIdsToAdd().isEmpty()) {
            List<SessionTemplatePublishedToUserGroup> sessionTemplateToPublish = new ArrayList<>();
            for (String sessionTemplateId : request.getSessionTemplateIdsToAdd()) {
                if (sessionTemplateId == null) {
                    continue;
                }

                SessionTemplatePublishedToUserGroup sessionTemplate = new SessionTemplatePublishedToUserGroup(
                        new SessionTemplate().id(sessionTemplateId),
                        editedUserGroup);

                sessionTemplateToPublish.add(sessionTemplate);
            }
            log.info("Publishing {} session templates to group {}", sessionTemplateToPublish.size(), editedUserGroup.getUserGroupId());
            sessionTemplateService.publishSessionTemplatesToGroups(sessionTemplateToPublish);
        }

        return editedUserGroup;
    }

    public UserGroupEntity createUserGroupOrReturnIfExists(String groupId, String displayName, boolean isImported) {
        Optional<UserGroupEntity> userGroup = userGroupRepository.findById(groupId);
        if (userGroup.isPresent()) {
            log.info("Found exiting user group {}", userGroup.get());
            return updateExistingUserGroupEntity(userGroup.get(), userGroup.get().getDisplayName(), isImported);
        }

        UserGroupEntity group = createNewUserGroupEntity(groupId, displayName, isImported);
        log.info("Creating new user group {}", group);
        userGroupRepository.save(group);
        return group;
    }

    public UserGroupEntity createNewUserGroupEntity(String groupId, @Nullable String displayName, boolean isImported) {
        OffsetDateTime now = OffsetDateTime.now();
        UserGroup group = new UserGroupEntity()
                .userGroupId(groupId)
                .displayName(Optional.ofNullable(displayName).orElse(groupId))
                .isImported(isImported)
                .creationTime(now)
                .lastModifiedTime(now);

        return (UserGroupEntity) group;
    }

    public UserGroupEntity updateExistingUserGroupEntity(UserGroupEntity existingUserGroup, String displayName, boolean isImported) {
        existingUserGroup.setDisplayName(displayName);
        existingUserGroup.setIsImported(isImported);
        existingUserGroup.setLastModifiedTime(OffsetDateTime.now());

        return existingUserGroup;
    }

    public DescribeUserGroupsResponse describeUserGroups(DescribeUserGroupsRequestData request) {
        Sort sort = Sort.unsorted();
        int maxResults = defaultMaxResults;

        try {
            if (request.getSortToken() != null) {
                sort = Sort.by(Sort.Direction.fromString(request.getSortToken().getOperator().toString()), request.getSortToken().getKey());
            }

            if (request.getMaxResults() != null) {
                maxResults = request.getMaxResults();
            }
        } catch (Exception e) {
            throw new BadRequestException(e);
        }

        NextToken nextToken = NextToken.deserialize(request.getNextToken(), UserGroupEntity.class);
        RepositoryRequest repositoryRequest = RepositoryRequest.builder()
                .nextToken(nextToken)
                .maxResults(maxResults)
                .sort(sort)
                .clazz(UserGroupEntity.class)
                .build();

        RepositoryResponse<UserGroupEntity> repositoryResponse = userGroupRepository.findAll(repositoryRequest);
        List<UserGroupEntity> userGroups = repositoryResponse.getItems();
        userGroups = userGroupFilter.getFiltered(request, userGroups);

        return new DescribeUserGroupsResponse().userGroups(
                userGroups.parallelStream()
                        .map(this::populateUserGroupWithUsers)
                        .collect(Collectors.toList()))
                .nextToken(NextToken.serialize(repositoryResponse.getNextToken(), UserGroupEntity.class));
    }

    public UserGroup populateUserGroupWithUsers(UserGroup userGroup) {
        userGroup.setUserIds(getUserIdsForGroup(userGroup.getUserGroupId()));
        return userGroup;
    }

    public List<String> getUserIdsForGroup(String userGroupId) {
        List<UserGroupUserMembership> userGroupUserMemberships = userGroupUserMembershipRepository.findByUserGroupUserGroupId(userGroupId);
        return userGroupUserMemberships
                .stream()
                .map(userGroupUserMembership -> userGroupUserMembership.getId().getUserId())
                .collect(Collectors.toList());
    }

    public boolean addUserToGroup(String userId, String groupId) {
        UserEntity user = (UserEntity) (new UserEntity().userId(userId));
        UserGroupEntity group = (UserGroupEntity) (new UserGroupEntity().userGroupId(groupId));
        UserGroupUserMembership membership = new UserGroupUserMembership(group, user);

        userGroupUserMembershipRepository.save(membership);
        return true;
    }

    public Iterable<UserGroupUserMembership> getUserGroupUserMemberships() {
        return userGroupUserMembershipRepository.findAll();
    }

    public boolean deleteUserGroups(List<String> userGroupIds) {
        userGroupRepository.deleteAllById(userGroupIds);
        return true;
    }

    public boolean removeMembersFromGroup(List<UserGroupUser> members) {
        userGroupUserMembershipRepository.deleteAllById(members);
        return true;
    }

    public boolean addMembersToGroup(List<UserGroupUserMembership> members) {
        if (members.isEmpty()) {
            return false;
        }
        userGroupUserMembershipRepository.saveAll(members);
        return true;
    }
}
