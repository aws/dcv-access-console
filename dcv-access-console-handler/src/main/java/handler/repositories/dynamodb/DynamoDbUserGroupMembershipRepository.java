// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.dynamodb;

import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.UserGroupUserMembership;
import handler.persistence.UserGroupUser;
import handler.repositories.SessionTemplatePublishedToUserGroupRepository;
import handler.repositories.UserGroupUserMembershipRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo;

@Repository
@ConditionalOnProperty(name = "persistence-db", havingValue = "dynamodb")
public class DynamoDbUserGroupMembershipRepository extends DynamoDbRepository<UserGroupUserMembership, UserGroupUser> implements UserGroupUserMembershipRepository {
    private final DynamoDbTable<UserGroupUserMembership> table;

    public DynamoDbUserGroupMembershipRepository(DynamoDbEnhancedClient client,
                                                 DynamoDbTable<UserGroupUserMembership> table,
                                                 ObjectProvider<WriteBatch.Builder<UserGroupUserMembership>> writeBuilder,
                                                 ObjectProvider<ReadBatch.Builder<UserGroupUserMembership>> readBuilder) {
        super(client, table, writeBuilder, readBuilder);
        this.table = table;
    }

    @Override
    public List<UserGroupUserMembership> findByUserGroupUserGroupId(String userGroupId) {
        List<UserGroupUserMembership> result = new ArrayList<>();
        table.query(keyEqualTo(k -> k.partitionValue(userGroupId))).stream().forEach(page -> result.addAll(page.items()));
        return result;
    }

    @Override
    public List<UserGroupUserMembership> findByUserUserId(String userId) {
        List<UserGroupUserMembership> result = new ArrayList<>();
        table.query(keyEqualTo(k -> k.partitionValue(userId))).stream().forEach(page -> result.addAll(page.items()));
        return result;
    }
}
