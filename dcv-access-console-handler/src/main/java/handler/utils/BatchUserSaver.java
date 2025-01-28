// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.utils;

import handler.persistence.UserEntity;
import handler.persistence.UserGroupUserMembership;
import handler.repositories.UserGroupUserMembershipRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.context.annotation.RequestScope;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequestScope
public class BatchUserSaver {
    private final ConcurrentLruCache<String, Optional<UserEntity>> userRepositoryCache;
    private final CrudRepository<UserEntity, String> userRepository;
    private final UserGroupUserMembershipRepository userGroupUserMembershipRepository;

    private final HashMap<String, UserEntity> batchSaveUsersMap;
    private final HashMap<String, List<UserGroupUserMembership>> batchUserGroupUserMembershipsMap;

    @Getter
    private final List<String> successfulUsersList;
    @Getter
    private final List<String> unsuccessfulUsersList;

    private final int maxBatchSaveSize;

    public BatchUserSaver(@Value("${users-batch-save-size:100}") int maxBatchSaveSize,
                          @Value("${import-users-cache-size:100}") int cacheSize,
                          CrudRepository<UserEntity, String> userRepository,
                          UserGroupUserMembershipRepository userGroupUserMembershipRepository) {
        this.batchSaveUsersMap = new HashMap<>();
        this.batchUserGroupUserMembershipsMap = new HashMap<>();
        this.successfulUsersList = new ArrayList<>();
        this.unsuccessfulUsersList = new ArrayList<>();
        this.userRepository = userRepository;
        this.userGroupUserMembershipRepository = userGroupUserMembershipRepository;
        this.maxBatchSaveSize = maxBatchSaveSize;

        this.userRepositoryCache = new ConcurrentLruCache<>(cacheSize, userRepository::findById);
    }

    public void saveUser(UserEntity user, boolean overwriteSavedUsers) {
        if (overwriteSavedUsers) {
            // We are overwriting saved users. Try to update the user in the DB if it exists, otherwise add a new user
            userRepository.findById(user.getUserId()).ifPresentOrElse(
                    existingUser -> batchSaveUsersMap.put(user.getUserId(), updateExistingUser(existingUser, user)),
                    () -> batchSaveUsersMap.put(user.getUserId(), user)
            );
        } else if (this.batchSaveUsersMap.containsKey(user.getUserId()) || this.userRepositoryCache.get(user.getUserId()).isPresent()) {
            // The users existed, and we're not overwriting, so this user was unsuccessful
            unsuccessfulUsersList.add(user.getUserId());
        } else {
            // The user isn't in the batch map or the DB, we can add it
            batchSaveUsersMap.put(user.getUserId(), user);
        }

        sendBatchIfReady();
    }

    public void addUserGroupUserMemberships(String userId, List<UserGroupUserMembership> memberships, boolean overwriteSavedGroups) {
        if (overwriteSavedGroups) {
            log.info("Overwriting groups for user {}", userId);
            List<UserGroupUserMembership> existingUserGroups = userGroupUserMembershipRepository.findByUserUserId(userId);
            userGroupUserMembershipRepository.deleteAll(existingUserGroups);
            batchUserGroupUserMembershipsMap.put(userId, memberships);
        } else {
            if (batchUserGroupUserMembershipsMap.containsKey(userId)) {
                batchUserGroupUserMembershipsMap.get(userId).addAll(memberships);
            } else {
                batchUserGroupUserMembershipsMap.put(userId, memberships);
            }
        }
    }

    public void sendBatchAndClear() {
        if (!batchSaveUsersMap.isEmpty()) {
            userRepository.saveAll(batchSaveUsersMap.values());
            successfulUsersList.addAll(batchSaveUsersMap.keySet());
            batchSaveUsersMap.clear();
        }
        if (!batchUserGroupUserMembershipsMap.isEmpty()) {
            userGroupUserMembershipRepository.saveAll(batchUserGroupUserMembershipsMap
                    .values()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList()));
            batchUserGroupUserMembershipsMap.clear();
        }
    }

    private void sendBatchIfReady() {
        if(batchSaveUsersMap.size() >= this.maxBatchSaveSize) {
            sendBatchAndClear();
        }
    }

    private UserEntity updateExistingUser(UserEntity existingUser, UserEntity newUser) {
        existingUser.setDisplayName(newUser.getDisplayName());
        existingUser.setRole(newUser.getRole());
        existingUser.setIsImported(newUser.getIsImported());
        existingUser.setLastModifiedTime(OffsetDateTime.now());
        return existingUser;
    }
}
