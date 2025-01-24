package handler.services;

import handler.authorization.engines.entities.SetShareListResponse;
import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.model.CreateSessionTemplateRequestData;
import handler.model.CreateSessionTemplateResponse;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeSessionTemplatesResponse;
import handler.model.DescribeUsersRequestData;
import handler.model.FilterToken;
import handler.model.FilterTokenStrict;
import handler.model.PublishSessionTemplateResponse;
import handler.model.SessionTemplate;
import handler.model.UnpublishSessionTemplateResponse;
import handler.model.User;
import handler.persistence.SessionTemplatePublishedToUser;
import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.SessionTemplateUserGroupId;
import handler.persistence.SessionTemplateUserId;
import handler.persistence.UserEntity;
import handler.persistence.UserGroupEntity;
import handler.persistence.UserGroupUser;
import handler.persistence.UserGroupUserMembership;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.SessionTemplatePublishedToUserGroupRepository;
import handler.repositories.SessionTemplatePublishedToUserRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.NextToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionTemplateService {
    private final PagingAndSortingCrudRepository<SessionTemplate, String> sessionTemplateRepository;
    private final SessionTemplatePublishedToUserRepository sessionTemplatePublishedToUserRepository;
    private final SessionTemplatePublishedToUserGroupRepository sessionTemplatePublishedToUserGroupRepository;
    private final BrokerClient brokerClient;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    public CreateSessionTemplateResponse saveSessionTemplate(SessionTemplate sessionTemplate, CreateSessionTemplateRequestData request, boolean ignoreExisting, String username) {
        brokerClient.validateSessionTemplate(request, ignoreExisting);

        try {
            if (sessionTemplate.getId() == null) {
                sessionTemplate.setId(UUID.nameUUIDFromBytes(request.getName().getBytes()).toString());
            }
            sessionTemplate.setName(request.getName());
            sessionTemplate.setDescription(request.getDescription());
            sessionTemplate.setOsFamily(request.getOsFamily().getValue());
            sessionTemplate.setOsVersions(request.getOsVersions());
            sessionTemplate.setInstanceIds(request.getInstanceIds());
            sessionTemplate.setInstanceTypes(request.getInstanceTypes());
            sessionTemplate.setInstanceRegions(request.getInstanceRegions());
            sessionTemplate.setHostNumberOfCpus(request.getHostNumberOfCpus());
            sessionTemplate.setHostMemoryTotalBytes(request.getHostMemoryTotalBytes());
            sessionTemplate.setType(request.getType().getValue());
            sessionTemplate.setDcvGlEnabled(request.getDcvGlEnabled());
            if (StringUtils.isNotBlank(request.getRequirements())) {
                sessionTemplate.setRequirements(request.getRequirements());
            }
            if (StringUtils.isNotBlank(request.getAutorunFile())) {
                sessionTemplate.setAutorunFile(request.getAutorunFile());
            }
            sessionTemplate.setAutorunFileArguments(request.getAutorunFileArguments());
            sessionTemplate.setMaxConcurrentClients(request.getMaxConcurrentClients());
            sessionTemplate.setInitFile(request.getInitFile());
            sessionTemplate.setStorageRoot(request.getStorageRoot());
            sessionTemplate.setPermissionsFile(request.getPermissionsFile());
            sessionTemplate.setEnqueueRequest(request.getEnqueueRequest());
            sessionTemplate.setDisableRetryOnFailure(request.getDisableRetryOnFailure());

            if (sessionTemplate.getCreatedBy() == null ) {
                sessionTemplate.setCreatedBy(username);
            }
            sessionTemplate.setLastModifiedBy(username);
            if (sessionTemplate.getCreationTime() == null) {
                sessionTemplate.setCreationTime(OffsetDateTime.now());
                sessionTemplate.setLastModifiedTime(sessionTemplate.getCreationTime());
            } else {
                sessionTemplate.setLastModifiedTime(OffsetDateTime.now());
            }
        } catch (Exception e) {
            throw new BadRequestException(e);
        }

        return new CreateSessionTemplateResponse().sessionTemplate(sessionTemplateRepository.save(sessionTemplate));
    }

    public SessionTemplate getUpdatedNameSessionTemplate(String sessionTemplateId, String name) {
        Optional<SessionTemplate> sessionTemplate = sessionTemplateRepository.findById(sessionTemplateId);
        if (sessionTemplate.isEmpty()) {
            throw new MissingResourceException("Session Template ID " + sessionTemplateId + " not found", SessionTemplate.class.getName(), sessionTemplateId);
        }

        String uuid = UUID.nameUUIDFromBytes(name.getBytes()).toString();
        //Name has changed so a new session template must be created
        if (!sessionTemplateId.equals(uuid)) {
            return sessionTemplate.get().id(null);
        }

        return sessionTemplate.get();
    }

    public DescribeSessionTemplatesResponse describeSessionTemplates(DescribeSessionTemplatesRequestData request) {
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

        NextToken nextToken = NextToken.deserialize(request.getNextToken(), SessionTemplate.class);
        RepositoryRequest repositoryRequest = RepositoryRequest.builder()
                .nextToken(nextToken)
                .maxResults(maxResults)
                .sort(sort)
                .clazz(SessionTemplate.class)
                .build();

        RepositoryResponse<SessionTemplate> repositoryResponse = sessionTemplateRepository.findAll(repositoryRequest);


        return new DescribeSessionTemplatesResponse()
                .sessionTemplates(repositoryResponse.getItems())
                .nextToken(NextToken.serialize(repositoryResponse.getNextToken(), SessionTemplate.class));

    }

    public List<SessionTemplate> filterByGroupId(DescribeSessionTemplatesRequestData request, List<SessionTemplate> sessionTemplates) {
        List<FilterTokenStrict> filterTokens = request.getGroupsSharedWith();
        if (filterTokens == null) {
            log.info("Not filtering Session Templates by Group Publishing");
            return sessionTemplates;
        }
        log.info("Filtering Session Templates by group ID. Found {} Group ID(s) to filter by", filterTokens.size());

        Set<String> filteredSessionTemplates = new HashSet<>();

        for (SessionTemplate sessionTemplate : sessionTemplates) {
            for (FilterTokenStrict filterToken : filterTokens) {
                log.info("Checking if session template {} is {} to group {}",
                        sessionTemplate.getId(),
                        filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.EQUAL) ? "published" : "not published",
                        filterToken.getValue());

                SessionTemplateUserGroupId sessionTemplateUserGroupId = new SessionTemplateUserGroupId();
                sessionTemplateUserGroupId.setUserGroupId(filterToken.getValue());
                sessionTemplateUserGroupId.setSessionTemplateId(sessionTemplate.getId());
                boolean exists = sessionTemplatePublishedToUserGroupRepository.existsById(sessionTemplateUserGroupId);

                if (filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.EQUAL) && exists) {
                    log.info("Session Template {} is published to group {}", sessionTemplate.getId(), filterToken.getValue());
                    filteredSessionTemplates.add(sessionTemplate.getId());
                } else if (filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.NOT_EQUAL) && !exists) {
                    log.info("Session Template {} is not published to group {}", sessionTemplate.getId(), filterToken.getValue());
                    filteredSessionTemplates.add(sessionTemplate.getId());
                } else {
                    log.info("Session template {} does not fulfill GroupSharedWith filter token", sessionTemplate.getId());
                }
            }
        }

        return sessionTemplates.parallelStream().filter(sessionTemplate -> filteredSessionTemplates.contains(sessionTemplate.getId())).toList();
    }

    public List<SessionTemplate> filterByUserId(DescribeSessionTemplatesRequestData request, List<SessionTemplate> sessionTemplates) {
        List<FilterTokenStrict> filterTokens = request.getUsersSharedWith();
        if (filterTokens == null) {
            log.debug("Not filtering Session Templates by User Publishing");
            return sessionTemplates;
        }
        log.info("Filtering Session Templates by user ID. Found {} Group ID(s) to filter by", filterTokens.size());
        Set<String> filteredSessionTemplates = new HashSet<>();

        for (SessionTemplate sessionTemplate : sessionTemplates) {
            for (FilterTokenStrict filterToken : filterTokens) {
                log.debug("Checking if session template {} is {} to user {}",
                        sessionTemplate.getId(),
                        filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.EQUAL) ? "published" : "not published",
                        filterToken.getValue());

                SessionTemplateUserId sessionTemplateUserId = new SessionTemplateUserId();
                sessionTemplateUserId.setUserId(filterToken.getValue());
                sessionTemplateUserId.setSessionTemplateId(sessionTemplate.getId());
                boolean exists = sessionTemplatePublishedToUserRepository.existsById(sessionTemplateUserId);

                if (filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.EQUAL) && exists) {
                    log.debug("Session Template {} is published to user {}", sessionTemplate.getId(), filterToken.getValue());
                    filteredSessionTemplates.add(sessionTemplate.getId());
                } else if (filterToken.getOperator().equals(FilterTokenStrict.OperatorEnum.NOT_EQUAL) && !exists) {
                    log.debug("Session Template {} is not published to user {}", sessionTemplate.getId(), filterToken.getValue());
                    filteredSessionTemplates.add(sessionTemplate.getId());
                } else {
                    log.debug("Session template {} does not fulfill GroupSharedWith filter token", sessionTemplate.getId());
                }
            }
        }

        return sessionTemplates.parallelStream().filter(sessionTemplate -> filteredSessionTemplates.contains(sessionTemplate.getId())).toList();
    }

    public RepositoryResponse<SessionTemplatePublishedToUserGroup> getSessionTemplatesPublishedToUserGroup(String userGroupId, RepositoryRequest repositoryRequest) {
        return sessionTemplatePublishedToUserGroupRepository
                .findByUserGroupUserGroupId(userGroupId, repositoryRequest);
    }

    public List<SessionTemplatePublishedToUserGroup> getSessionTemplatesPublishedToUserGroup(String userGroupId) {
        return sessionTemplatePublishedToUserGroupRepository.findByUserGroupUserGroupId(userGroupId);
    }

    public boolean removeUserGroupsFromSessionTemplate(List<SessionTemplatePublishedToUserGroup> sessionTemplatePublishedToUserGroups) {
        if (sessionTemplatePublishedToUserGroups.isEmpty()) {
            return false;
        }
        sessionTemplatePublishedToUserGroupRepository.deleteAll(sessionTemplatePublishedToUserGroups);
        return true;
    }

    public void deleteSessionTemplate(String sessionTemplateId) {
        if(sessionTemplateId != null) {
            sessionTemplateRepository.deleteById(sessionTemplateId);
            return;
        }

        throw new BadRequestException("Delete SessionTemplate failed: Id is invalid/null");
    }

    public void deleteSessionTemplates(List<String> sessionTemplateIds) {
        sessionTemplateRepository.deleteAllById(sessionTemplateIds);
    }

    // Replaces the share list of the session templates with this list of user
    public SetShareListResponse setSessionTemplateShareList(String sessionTemplateId, List<String> userIds, List<String> groupIds) {
        SessionTemplate sessionTemplate = new SessionTemplate().id(sessionTemplateId);
        SetShareListResponse response = SetShareListResponse.builder()
                .successfulGroups(new ArrayList<>()).successfulUsers(new ArrayList<>())
                .unSuccessfulGroups(new ArrayList<>()).unSuccessfulUsers(new ArrayList<>()).build();

        Set<String> userIdsSet = new HashSet<>(userIds);
        Set<String> groupIdsSet = new HashSet<>(groupIds);
        Set<String> existingUserIds = new HashSet<>(getUserIdsBySessionTemplate(sessionTemplateId));
        Set<String> existingGroupIds = new HashSet<>(getUserGroupIdsBySessionTemplate(sessionTemplateId));
        response.getSuccessfulUsers().addAll(userIdsSet.stream().filter(existingUserIds::contains).toList());
        response.getSuccessfulGroups().addAll(groupIdsSet.stream().filter(existingGroupIds::contains).toList());

        //Adding new users
        List<String> newUserIds = userIdsSet.stream().filter(userId -> !existingUserIds.contains(userId)).toList();
        if(!newUserIds.isEmpty()) {
            List<SessionTemplatePublishedToUser> sessionTemplatePublishedToUsers = new ArrayList<>();
            log.info("Adding {} new users to Session Template share list", newUserIds.size());
            for(String userId: newUserIds) {
                SessionTemplatePublishedToUser sessionTemplatePublishedToUser = new SessionTemplatePublishedToUser(sessionTemplate, (UserEntity) new UserEntity().userId(userId));
                sessionTemplatePublishedToUsers.add(sessionTemplatePublishedToUser);
            }
            sessionTemplatePublishedToUserRepository.saveAll(sessionTemplatePublishedToUsers);
            List<String> successfulUserIds = sessionTemplatePublishedToUsers.stream().map(SessionTemplatePublishedToUser::getId).map(SessionTemplateUserId::getUserId).toList();
            response.getSuccessfulUsers().addAll(successfulUserIds);
            response.getUnSuccessfulUsers().addAll(newUserIds.stream().filter(userId -> !successfulUserIds.contains(userId)).toList());
        }

        //Removing old users
        List<String> oldUserIds = existingUserIds.stream().filter(userId -> !userIdsSet.contains(userId)).toList();
        if(!oldUserIds.isEmpty()) {
            List<SessionTemplateUserId> sessionTemplateUserIds = new ArrayList<>();
            log.info("Removing {} users from Session Template share list", oldUserIds.size());
            for(String userId: oldUserIds) {
                SessionTemplateUserId sessionTemplateUserId = new SessionTemplateUserId();
                sessionTemplateUserId.setSessionTemplateId(sessionTemplateId);
                sessionTemplateUserId.setUserId(userId);
                sessionTemplateUserIds.add(sessionTemplateUserId);
            }
            sessionTemplatePublishedToUserRepository.deleteAllById(sessionTemplateUserIds);
        }

        //Adding new groups
        List<String> newGroupIds = groupIdsSet.stream().filter(groupId -> !existingGroupIds.contains(groupId)).toList();
        if(!newGroupIds.isEmpty()) {
            List<SessionTemplatePublishedToUserGroup> sessionTemplatePublishedToUserGroups = new ArrayList<>();
            log.info("Adding {} groups to Session Template share list", newGroupIds.size());
            for(String groupId: newGroupIds) {
                SessionTemplatePublishedToUserGroup sessionTemplatePublishedToUserGroup = new SessionTemplatePublishedToUserGroup(sessionTemplate, (UserGroupEntity) new UserGroupEntity().userGroupId(groupId));
                sessionTemplatePublishedToUserGroups.add(sessionTemplatePublishedToUserGroup);
            }
            sessionTemplatePublishedToUserGroupRepository.saveAll(sessionTemplatePublishedToUserGroups);
            List<String> successfulGroupIds = sessionTemplatePublishedToUserGroups.stream().map(SessionTemplatePublishedToUserGroup::getId).map(SessionTemplateUserGroupId::getUserGroupId).toList();
            response.getSuccessfulGroups().addAll(successfulGroupIds);
            response.getUnSuccessfulGroups().addAll(newGroupIds.stream().filter(groupId -> !successfulGroupIds.contains(groupId)).toList());
        }

        //Removing old users
        List<String> oldGroupIds = existingGroupIds.stream().filter(groupId -> !groupIdsSet.contains(groupId)).toList();
        if(!oldGroupIds.isEmpty()) {
            List<SessionTemplateUserGroupId> sessionTemplateUserGroupIds = new ArrayList<>();
            log.info("Removing {} groups from Session Template share list", oldGroupIds.size());
            for(String groupId: oldGroupIds) {
                SessionTemplateUserGroupId sessionTemplateUserGroupId = new SessionTemplateUserGroupId();
                sessionTemplateUserGroupId.setSessionTemplateId(sessionTemplateId);
                sessionTemplateUserGroupId.setUserGroupId(groupId);
                sessionTemplateUserGroupIds.add(sessionTemplateUserGroupId);
            }
            sessionTemplatePublishedToUserGroupRepository.deleteAllById(sessionTemplateUserGroupIds);
        }

        return response;
    }

    // Adds the list of users and the list of groups to the Session Template Share List
    public PublishSessionTemplateResponse publishSessionTemplate(String sessionTemplateId, List<String> userIds, List<String> groupIds) {
        SessionTemplate sessionTemplate = new SessionTemplate().id(sessionTemplateId);
        PublishSessionTemplateResponse response = new PublishSessionTemplateResponse();
        log.info("UserIds: {}", userIds);
        log.info("GroupIds: {}", groupIds);

        List<SessionTemplatePublishedToUser> sessionTemplatePublishedToUsers = new ArrayList<>();
        for(String userId : userIds) {
            SessionTemplatePublishedToUser sessionTemplatePublishedToUser = new SessionTemplatePublishedToUser(sessionTemplate, (UserEntity) new UserEntity().userId(userId));
            sessionTemplatePublishedToUsers.add(sessionTemplatePublishedToUser);

            response.addSuccessfulUsersListItem(userId);
            log.info("Added user {} to session template {}", userId, sessionTemplateId);
        }
        if (!sessionTemplatePublishedToUsers.isEmpty()) {
            publishSessionTemplatesToUsers(sessionTemplatePublishedToUsers);
        }

        List<SessionTemplatePublishedToUserGroup> sessionTemplatePublishedToUserGroups = new ArrayList<>();
        for (String groupID : groupIds) {
            SessionTemplatePublishedToUserGroup sessionTemplatePublishedToUserGroup = new SessionTemplatePublishedToUserGroup(sessionTemplate, (UserGroupEntity) new UserGroupEntity().userGroupId(groupID));
            sessionTemplatePublishedToUserGroups.add(sessionTemplatePublishedToUserGroup);
            response.addSuccessfulGroupsListItem(groupID);
            log.info("Added group {} to session template {}", groupID, sessionTemplateId);
        }
        if (!sessionTemplatePublishedToUserGroups.isEmpty()) {
            publishSessionTemplatesToGroups(sessionTemplatePublishedToUserGroups);
        }

        return response;
    }

    public boolean publishSessionTemplatesToGroups(List<SessionTemplatePublishedToUserGroup> sessionTemplatePublishedToUserGroups) {
        if (sessionTemplatePublishedToUserGroups.isEmpty()) {
            return false;
        }
        sessionTemplatePublishedToUserGroupRepository.saveAll(sessionTemplatePublishedToUserGroups);
        return true;
    }

    public boolean publishSessionTemplatesToUsers(List<SessionTemplatePublishedToUser> sessionTemplatePublishedToUsers) {
        if (sessionTemplatePublishedToUsers.isEmpty()) {
            return false;
        }
        sessionTemplatePublishedToUserRepository.saveAll(sessionTemplatePublishedToUsers);
        return true;
    }

    public UnpublishSessionTemplateResponse unpublishSessionTemplate(String sessionTemplateId, List<String> userIds, List<String> groupIds) {
        UnpublishSessionTemplateResponse response = new UnpublishSessionTemplateResponse();
        if (userIds != null && !userIds.isEmpty()) {
            List<SessionTemplateUserId> publishedUsers = new ArrayList<>();
            for (String userId : userIds) {
                SessionTemplateUserId sessionTemplateUserId = new SessionTemplateUserId();
                sessionTemplateUserId.setSessionTemplateId(sessionTemplateId);
                sessionTemplateUserId.setUserId(userId);
                publishedUsers.add(sessionTemplateUserId);

                response.addSuccessfulUsersListItem(userId);
            }
            log.info("Unpublishing {} users from session template {}", publishedUsers.size(), sessionTemplateId);
            sessionTemplatePublishedToUserRepository.deleteAllById(publishedUsers);
        }

        if (groupIds != null && !groupIds.isEmpty()) {
            List<SessionTemplateUserGroupId> publishedGroups = new ArrayList<>();
            for (String groupId : groupIds) {
                SessionTemplateUserGroupId sessionTemplateUserGroupId = new SessionTemplateUserGroupId();
                sessionTemplateUserGroupId.setSessionTemplateId(sessionTemplateId);
                sessionTemplateUserGroupId.setUserGroupId(groupId);
                publishedGroups.add(sessionTemplateUserGroupId);

                response.addSuccessfulGroupsListItem(groupId);
            }
            log.info("Unpublishing {} groups from session template {}", publishedGroups.size(), sessionTemplateId);
            sessionTemplatePublishedToUserGroupRepository.deleteAllById(publishedGroups);
        }

        return response;
    }

    public List<String> getUserIdsBySessionTemplate(String sessionTemplateId) {
        return sessionTemplatePublishedToUserRepository.findBySessionTemplateId(sessionTemplateId).stream().map(SessionTemplatePublishedToUser::getId).map(SessionTemplateUserId::getUserId).toList();
    }

    public List<String> getUserGroupIdsBySessionTemplate(String sessionTemplateId) {
        return sessionTemplatePublishedToUserGroupRepository.findBySessionTemplateId(sessionTemplateId).stream().map(sessionTemplatePublishedToUserGroup -> sessionTemplatePublishedToUserGroup.getId().getUserGroupId()).toList();
    }
}
