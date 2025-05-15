// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.services;

import handler.authorization.engines.entities.UserCsvEntity;
import handler.exceptions.BadRequestException;
import handler.model.DescribeUsersRequestData;
import handler.model.DescribeUsersResponse;
import handler.model.FilterTokenStrict;
import handler.model.ImportUsersResponse;
import handler.model.User;
import handler.persistence.UserEntity;
import handler.persistence.UserGroupEntity;
import handler.persistence.UserGroupUser;
import handler.persistence.UserGroupUserMembership;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.UserGroupUserMembershipRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.BatchUserSaver;
import handler.utils.Filter;
import handler.utils.NextToken;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserGroupUserMembershipRepository userGroupUserMembershipRepository;
    private final UserGroupService userGroupService;
    private final PagingAndSortingCrudRepository<UserEntity, String> userRepository;
    private final BatchUserSaver batcher;
    private final Filter<DescribeUsersRequestData, User> userFilter;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    @Value("${users-batch-save-size:100}")
    private int MAX_BATCH_SAVE_SIZE;

    public boolean createUser(String userId, String displayName, String role) {
        if(userRepository.existsById(userId)) {
            return false;
        }

        userRepository.save(getNewUserEntity(userId, displayName, role, false, null, false));
        log.info("Successfully added User {} to the persistence layer", userId);
        return true;
    }

    public User updateUser(String userId, Optional<String> loginUsername, String displayName) {
        UserEntity existingUser = userRepository.findById(userId).orElseThrow(() -> new MissingResourceException("Cannot update user as it does not exist", UserEntity.class.getName().toString(), userId));

        boolean changeFlag = false;
        if (loginUsername != null && !StringUtils.equals(existingUser.getLoginUsername(), loginUsername.get())) {
            existingUser.setLoginUsername(loginUsername.get());
            changeFlag = true;
        }

        if (!StringUtils.isEmpty(displayName)) {
            if (!StringUtils.equals(existingUser.getDisplayName(), displayName)) {
                existingUser.setDisplayName(displayName);
                changeFlag = true;
            }
        } else {
            log.warn("DisplayName not updated as it cannot be blank");
        }

        if (!changeFlag) {
            return null;
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Successfully updated User {} on the persistence layer", userId);
        return updatedUser;
    }


    private UserEntity getNewUserEntity(String userId, String displayName, String role, Boolean isDisabled, String disabledReason, Boolean isImported) {
        User user = new UserEntity().userId(userId);
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setIsImported(isImported);
        user.setIsDisabled(isDisabled);
        user.setDisabledReason(disabledReason);
        user.setCreationTime(OffsetDateTime.now());
        user.setLastModifiedTime(user.getCreationTime());
        return (UserEntity) user;
    }

    public DescribeUsersResponse describeUsers(DescribeUsersRequestData request) {
        Sort sort = Sort.unsorted();
        int maxResults = defaultMaxResults;

        try {

            if (request.getSortToken() != null) {
                sort = Sort.by(Direction.fromString(request.getSortToken().getOperator().toString()), request.getSortToken().getKey());
            }

            if (request.getMaxResults() != null) {
                maxResults = request.getMaxResults();
            }
        } catch (Exception e) {
            throw new BadRequestException(e);
        }

        NextToken nextToken = NextToken.deserialize(request.getNextToken(), UserEntity.class);
        RepositoryRequest repositoryRequest = RepositoryRequest.builder()
                .nextToken(nextToken)
                .maxResults(maxResults)
                .sort(sort)
                .clazz(UserEntity.class)
                .build();

        RepositoryResponse<UserEntity> repositoryResponse = userRepository.findAll(repositoryRequest);
        List<User> users = repositoryResponse.getItems().stream().map(u -> (User) u).toList();
        users = userFilter.getFiltered(request, users);
        users = filterByGroupId(request, users);

        return new DescribeUsersResponse()
                .users(users)
                .nextToken(NextToken.serialize(repositoryResponse.getNextToken(), UserEntity.class));
    }

    private List<User> filterByGroupId(DescribeUsersRequestData request, List<User> users) {
        List<FilterTokenStrict> userGroupIdFilterTokens = request.getUserGroupIds();
        if (userGroupIdFilterTokens == null || userGroupIdFilterTokens.isEmpty()) {
            log.debug("Not filtering Users by Group Membership");
            return users;
        }
        log.info("Filtering users by group ID. Found {} Group ID(s) to filter by", userGroupIdFilterTokens.size());
        Set<String> filteredUsers = new HashSet<>();

        for (User user : users) {
            for (FilterTokenStrict filterToken : userGroupIdFilterTokens) {

                log.debug("Checking if user {} is {} in group {}",
                        user.getUserId(),
                        filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.EQUAL) ? "present" : "not present",
                        filterToken.getValue());

                UserGroupUser userGroupUser = new UserGroupUser();
                userGroupUser.setUserGroupId(filterToken.getValue());
                userGroupUser.setUserId(user.getUserId());
                boolean exists = userGroupUserMembershipRepository.existsById(userGroupUser);

                if (filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.EQUAL) && exists) {
                    log.debug("User {} is in group {}", user.getUserId(), filterToken.getValue());
                    filteredUsers.add(user.getUserId());
                } else if (filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.NOT_EQUAL) && !exists) {
                    log.debug("User {} is not in group {}", user.getUserId(), filterToken.getValue());
                    filteredUsers.add(user.getUserId());
                } else {
                    log.debug("User {} does not fulfill UserGroupId filter token", user.getUserId());
                }
            }
        }

        return users.parallelStream().filter(user -> filteredUsers.contains(user.getUserId())).toList();
    }

    public void updateLastLoggedInTime(String userId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if(user.isPresent()) {
            user.get().setLastLoggedInTime(OffsetDateTime.now());
            userRepository.save(user.get());
            log.debug("Updated last logged in time for user {}", userId);
        }
    }

    public ImportUsersResponse importUsers(MultipartFile file, Boolean overwriteExistingUsers, Boolean overwriteGroups, List<String> roles, String defaultRole) throws IOException {
        if (file == null) {
            throw new BadRequestException("ImportUsers failed: File is invalid/null");
        }

        ImportUsersResponse response = new ImportUsersResponse().successfulUsersList(new ArrayList<>())
                .unsuccessfulUsersList(new ArrayList<>());

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CsvToBean<UserCsvEntity> csvToBean = new CsvToBeanBuilder<UserCsvEntity>(reader)
                    .withType(UserCsvEntity.class)
                    .build();

            for (UserCsvEntity user : csvToBean) {
                String userId = user.getUserId();
                if (StringUtils.isBlank(userId)) {
                    log.warn("Ignoring {} since userId is blank", userId);
                    batcher.getUnsuccessfulUsersList().add(userId);
                    continue;
                }
                if (userId.length() > 255) {
                    log.warn("Ignoring {} since userId length should be at most 255 characters", userId);
                    batcher.getUnsuccessfulUsersList().add(userId);
                    continue;
                }

                String displayName = userId;
                String role = defaultRole;
                if (!StringUtils.isBlank(user.getDisplayName()) && user.getDisplayName().length() < 255) {
                    displayName = user.getDisplayName();
                }
                if (!StringUtils.isBlank(user.getRole()) && roles.contains(user.getRole())) {
                    role = user.getRole();
                }
                UserEntity newUser = getNewUserEntity(userId, displayName, role, false, null, true);

                log.info("Starting user group import");

                // Get a list of all the Group Entities that the User should be a member of
                List<UserGroupEntity> userGroups = new ArrayList<>();
                log.info("Parsed list of groups {} for user {}", userGroups, user);
                if (user.getGroups() != null && !user.getGroups().isEmpty()) {
                    for (String groupId : new HashSet<>(user.getGroups())) {
                        if (StringUtils.isNotBlank(groupId)) {
                            log.debug("Parsed user {} is a member of group {}", userId, groupId);
                            userGroups.add(userGroupService.createUserGroupOrReturnIfExists(groupId, groupId, true));
                        }
                    }
                }
                if (!userGroups.isEmpty()) {
                    List<UserGroupUserMembership> memberships = userGroups.stream().map(userGroup -> new UserGroupUserMembership(userGroup, newUser)).collect(Collectors.toList());
                    log.info("Saving memberships: {}", memberships);
                    batcher.addUserGroupUserMemberships(userId, memberships, overwriteGroups);
                } else {
                    log.debug("No groups found for user {}", userId);
                }

                batcher.saveUser(newUser, overwriteExistingUsers);
            }
            batcher.sendBatchAndClear();
            batcher.getSuccessfulUsersList().forEach(response::addSuccessfulUsersListItem);
            batcher.getUnsuccessfulUsersList().forEach(response::addUnsuccessfulUsersListItem);
        }
        return response;
    }
}
