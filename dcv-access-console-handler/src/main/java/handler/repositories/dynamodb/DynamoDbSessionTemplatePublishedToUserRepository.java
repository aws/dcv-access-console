package handler.repositories.dynamodb;

import handler.persistence.SessionTemplatePublishedToUser;
import handler.persistence.SessionTemplateUserId;
import handler.repositories.SessionTemplatePublishedToUserRepository;
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
public class DynamoDbSessionTemplatePublishedToUserRepository extends DynamoDbRepository<SessionTemplatePublishedToUser, SessionTemplateUserId> implements SessionTemplatePublishedToUserRepository {
    private final DynamoDbTable<SessionTemplatePublishedToUser> table;

    public DynamoDbSessionTemplatePublishedToUserRepository(DynamoDbEnhancedClient client,
                                  DynamoDbTable<SessionTemplatePublishedToUser> table,
                                  ObjectProvider<WriteBatch.Builder<SessionTemplatePublishedToUser>> writeBuilder,
                                  ObjectProvider<ReadBatch.Builder<SessionTemplatePublishedToUser>> readBuilder) {
        super(client, table, writeBuilder, readBuilder);
        this.table = table;
    }

    @Override
    public List<SessionTemplatePublishedToUser> findBySessionTemplateId(String sessionTemplateId) {
        List<SessionTemplatePublishedToUser> result = new ArrayList<>();
        table.query(keyEqualTo(k -> k.partitionValue(sessionTemplateId))).stream().forEach(page -> result.addAll(page.items()));
        return result;
    }

    @Override
    public List<SessionTemplatePublishedToUser> findByUserUserId(String userId) {
        List<SessionTemplatePublishedToUser> result = new ArrayList<>();
        table.query(keyEqualTo(k -> k.partitionValue(userId))).stream().forEach(page -> result.addAll(page.items()));
        return result;
    }
}