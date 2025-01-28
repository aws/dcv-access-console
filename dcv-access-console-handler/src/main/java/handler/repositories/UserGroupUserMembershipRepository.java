// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories;

import handler.persistence.UserGroupUser;
import handler.persistence.UserGroupUserMembership;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface UserGroupUserMembershipRepository extends CrudRepository<UserGroupUserMembership, UserGroupUser> {

    public List<UserGroupUserMembership> findByUserGroupUserGroupId(String userGroupId);
    public List<UserGroupUserMembership> findByUserUserId(String userId);
}
