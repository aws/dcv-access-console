// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.dynamodb;

import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.SessionTemplateUserGroupId;
import handler.repositories.SessionTemplatePublishedToUserGroupRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.NextToken;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo;

@Repository
@ConditionalOnProperty(name = "persistence-db", havingValue = "dynamodb")
public class DynamoDbSessionTemplatePublishedToUserGroupRepository extends DynamoDbRepository<SessionTemplatePublishedToUserGroup, SessionTemplateUserGroupId> implements SessionTemplatePublishedToUserGroupRepository {
    private final DynamoDbTable<SessionTemplatePublishedToUserGroup> table;

    public DynamoDbSessionTemplatePublishedToUserGroupRepository(DynamoDbEnhancedClient client,
                                                                 DynamoDbTable<SessionTemplatePublishedToUserGroup> table,
                                                                 ObjectProvider<WriteBatch.Builder<SessionTemplatePublishedToUserGroup>> writeBuilder,
                                                                 ObjectProvider<ReadBatch.Builder<SessionTemplatePublishedToUserGroup>> readBuilder) {
        super(client, table, writeBuilder, readBuilder);
        this.table = table;
    }

    @Override
    public List<SessionTemplatePublishedToUserGroup> findBySessionTemplateId(String sessionTemplateId) {
        List<SessionTemplatePublishedToUserGroup> result = new ArrayList<>();
        table.query(keyEqualTo(k -> k.partitionValue(sessionTemplateId))).stream().forEach(page -> result.addAll(page.items()));
        return result;
    }

    @Override
    public List<SessionTemplatePublishedToUserGroup> findByUserGroupUserGroupId(String userGroupId) {
        List<SessionTemplatePublishedToUserGroup> result = new ArrayList<>();

        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                .partitionValue(userGroupId)
                .build()
        );
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        DynamoDbIndex<SessionTemplatePublishedToUserGroup> index = table.index("userGroupId");

        index.query(request)
                .stream()
                .forEach(page -> result.addAll(page.items()));

        return result;
    }

    private SdkIterable<Page<SessionTemplatePublishedToUserGroup>> findByUserGroupId(String groupId, Map<String, AttributeValue> startKey, int limit) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                .partitionValue(groupId)
                .build()
        );
        DynamoDbIndex<SessionTemplatePublishedToUserGroup> index = table.index("userGroupId");

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .exclusiveStartKey(startKey)
                .limit(limit)
                .queryConditional(queryConditional)
                .build();

        return index.query(request);
    }

    @Override
    public RepositoryResponse<SessionTemplatePublishedToUserGroup> findByUserGroupUserGroupId(String userGroupId, RepositoryRequest request) {
        Map<String, AttributeValue> startKey = request.getNextToken().getDynamoDbStartKey().orElse(null);
        Page<SessionTemplatePublishedToUserGroup> page = findByUserGroupId(userGroupId, startKey, request.getMaxResults()).stream().findFirst().get();

        List<SessionTemplatePublishedToUserGroup> items = page.items().subList(request.getNextToken().getPageOffset().getAsInt(), page.items().size());

        NextToken newNextToken = NextToken.from(page.lastEvaluatedKey(), request.getClazz());

        return RepositoryResponse.<SessionTemplatePublishedToUserGroup>builder().items(items).nextToken(newNextToken).build();
    }
}
