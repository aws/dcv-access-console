// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.mysql;

import handler.persistence.UserGroupUser;
import handler.persistence.UserGroupUserMembership;
import handler.repositories.UserGroupUserMembershipRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@ConditionalOnProperty(name = "persistence-db", havingValue = "mysql")
@Repository
public interface MySqlUserGroupMembershipRepository extends UserGroupUserMembershipRepository, MySqlRepository<UserGroupUserMembership, UserGroupUser> {
    @Override
    List<UserGroupUserMembership> findByUserGroupUserGroupId(String userGroupId);
}
