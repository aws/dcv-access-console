// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.mysql;

import handler.persistence.UserGroupEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "persistence-db", havingValue = "mysql")
@Repository
public interface MySqlUserGroupRepository extends MySqlRepository<UserGroupEntity, String> {
}
