// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.dynamodb;

import handler.persistence.UserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

@Repository
@ConditionalOnProperty(name = "persistence-db", havingValue = "dynamodb")
public class DynamoDbUserRepository extends DynamoDbRepository<UserEntity, String> {
    public DynamoDbUserRepository(DynamoDbEnhancedClient client,
                                  DynamoDbTable<UserEntity> table,
                                  ObjectProvider<WriteBatch.Builder<UserEntity>> writeBuilder,
                                  ObjectProvider<ReadBatch.Builder<UserEntity>> readBuilder) {
        super(client, table, writeBuilder, readBuilder);
    }
}
