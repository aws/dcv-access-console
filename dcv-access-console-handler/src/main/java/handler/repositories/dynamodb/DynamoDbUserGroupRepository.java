package handler.repositories.dynamodb;

import handler.persistence.UserGroupEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

@Repository
@ConditionalOnProperty(name = "persistence-db", havingValue = "dynamodb")
public class DynamoDbUserGroupRepository extends DynamoDbRepository<UserGroupEntity, String> {
    public DynamoDbUserGroupRepository(DynamoDbEnhancedClient client,
                                       DynamoDbTable<UserGroupEntity> table,
                                       ObjectProvider<WriteBatch.Builder<UserGroupEntity>> writeBuilder,
                                       ObjectProvider<ReadBatch.Builder<UserGroupEntity>> readBuilder) {
        super(client, table, writeBuilder, readBuilder);
    }
}
