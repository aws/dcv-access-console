// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.dynamodb;

import handler.persistence.SessionTemplateUserGroupId;
import handler.persistence.SessionTemplateUserId;
import handler.persistence.UserGroupUser;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.NextToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConditionalOnProperty(name = "persistence-db", havingValue = "dynamodb")
@AllArgsConstructor
@Slf4j
public class DynamoDbRepository<T, ID> implements PagingAndSortingCrudRepository<T, ID> {
    private final DynamoDbEnhancedClient client;
    private final DynamoDbTable<T> table;
    private ObjectProvider<WriteBatch.Builder<T>> writeBuilderProvider;
    private ObjectProvider<ReadBatch.Builder<T>> readBuilderProvider;

    @Override
    public <S extends T> S save(S entity) {
        table.putItem(entity);
        return entity;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        WriteBatch.Builder<T> writeBuilder = writeBuilderProvider.getIfAvailable();
        entities.forEach(writeBuilder::addPutItem);
        client.batchWriteItem(w -> w.addWriteBatch(writeBuilder.build()));
        return entities;
    }

    @Override
    public Optional<T> findById(ID id) {
        Key key;
        if(id instanceof SessionTemplateUserId sessionTemplateUserId) {
            key = Key.builder().partitionValue(sessionTemplateUserId.getSessionTemplateId()).sortValue(sessionTemplateUserId.getUserId()).build();
        } else if (id instanceof SessionTemplateUserGroupId sessionTemplateUserGroupId) {
            key = Key.builder().partitionValue(sessionTemplateUserGroupId.getSessionTemplateId()).sortValue(sessionTemplateUserGroupId.getUserGroupId()).build();
        } else if (id instanceof UserGroupUser userGroupUser) {
            key = Key.builder().partitionValue(userGroupUser.getUserGroupId()).sortValue(userGroupUser.getUserId()).build();
        } else if(id instanceof String string) {
            key = Key.builder().partitionValue(string).build();
        } else {
            throw new UnsupportedOperationException();
        }
        return Optional.ofNullable(table.getItem(key));
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        return table.scan().items().stream().toList();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        throw new UnsupportedOperationException();
    }

    public PageIterable<T> findAll(Map<String, AttributeValue> startKey, int limit) {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .exclusiveStartKey(startKey)
                .limit(limit)
                .build();

        return table.scan(request);
    }

    public RepositoryResponse<T> findAll(RepositoryRequest request) {
        Map<String, AttributeValue> startKey = request.getNextToken().getDynamoDbStartKey().orElse(null);
        PageIterable<T> iterable = findAll(startKey, request.getMaxResults());

        software.amazon.awssdk.enhanced.dynamodb.model.Page<T> pageOne = null;
        software.amazon.awssdk.enhanced.dynamodb.model.Page<T> pageTwo = null;
        for (software.amazon.awssdk.enhanced.dynamodb.model.Page<T> page : iterable) {
            if (pageOne == null) {
                pageOne = page;
                continue;
            }
            pageTwo = page;
            break;
        }

        NextToken newNextToken = null;
        if (pageTwo != null && pageTwo.items().size() > 0) {
            newNextToken = NextToken.from(pageOne.lastEvaluatedKey(), request.getClazz());;
        }

        List<T> items = pageOne.items().subList(request.getNextToken().getPageOffset().getAsInt(), pageOne.items().size());
        return RepositoryResponse.<T>builder().items(items).nextToken(newNextToken).build();
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        ReadBatch.Builder<T> readBuilder = readBuilderProvider.getIfAvailable();
        for(ID id: ids) {
            if(id instanceof SessionTemplateUserId sessionTemplateUserId) {
                readBuilder.addGetItem(Key.builder().partitionValue(sessionTemplateUserId.getSessionTemplateId()).sortValue(sessionTemplateUserId.getUserId()).build());
            } else if (id instanceof SessionTemplateUserGroupId sessionTemplateUserGroupId) {
                readBuilder.addGetItem(Key.builder().partitionValue(sessionTemplateUserGroupId.getSessionTemplateId()).sortValue(sessionTemplateUserGroupId.getUserGroupId()).build());
            } else if (id instanceof UserGroupUser userGroupUser) {
                readBuilder.addGetItem(Key.builder().partitionValue(userGroupUser.getUserGroupId()).sortValue(userGroupUser.getUserId()).build());
            } else if(id instanceof String string) {
                readBuilder.addGetItem(Key.builder().partitionValue(string).build());
            } else {
                throw new UnsupportedOperationException();
            }
        }

        ReadBatch readBatch = readBuilder.build();
        if (readBatch.keysAndAttributes() == null) {
            return List.of();
        }
        return client.batchGetItem(r -> r.addReadBatch(readBatch)).resultsForTable(table).stream().toList();
    }

    @Override
    public long count() {
        return table.scan().stream().count();
    }

    @Override
    public void deleteById(ID id) {
        Key key;
        if(id instanceof SessionTemplateUserId sessionTemplateUserId) {
            key = Key.builder().partitionValue(sessionTemplateUserId.getSessionTemplateId()).sortValue(sessionTemplateUserId.getUserId()).build();
        } else if (id instanceof SessionTemplateUserGroupId sessionTemplateUserGroupId) {
            key = Key.builder().partitionValue(sessionTemplateUserGroupId.getSessionTemplateId()).sortValue(sessionTemplateUserGroupId.getUserGroupId()).build();
        } else if (id instanceof UserGroupUser userGroupUser) {
            key = Key.builder().partitionValue(userGroupUser.getUserGroupId()).sortValue(userGroupUser.getUserId()).build();
        } else if(id instanceof String string) {
            key = Key.builder().partitionValue(string).build();
        } else {
            throw new UnsupportedOperationException();
        }
        table.deleteItem(key);
    }

    @Override
    public void delete(T entity) {
        table.deleteItem(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        WriteBatch.Builder<T> writeBuilder = writeBuilderProvider.getIfAvailable();
        for(ID id: ids) {
            if (id instanceof SessionTemplateUserId sessionTemplateUserId) {
                writeBuilder.addDeleteItem(Key.builder().partitionValue(sessionTemplateUserId.getSessionTemplateId()).sortValue(sessionTemplateUserId.getUserId()).build());
            } else if (id instanceof SessionTemplateUserGroupId sessionTemplateUserGroupId) {
                writeBuilder.addDeleteItem(Key.builder().partitionValue(sessionTemplateUserGroupId.getSessionTemplateId()).sortValue(sessionTemplateUserGroupId.getUserGroupId()).build());
            } else if (id instanceof UserGroupUser userGroupUser) {
                writeBuilder.addDeleteItem(Key.builder().partitionValue(userGroupUser.getUserGroupId()).sortValue(userGroupUser.getUserId()).build());
            } else if (id instanceof String string) {
                writeBuilder.addDeleteItem(Key.builder().partitionValue(string).build());
            } else {
                throw new UnsupportedOperationException("Unable to delete using ID of type: " + id);
            }
        }
        client.batchWriteItem(w -> w.addWriteBatch(writeBuilder.build()));
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        WriteBatch.Builder<T> writeBuilder = writeBuilderProvider.getIfAvailable();
        entities.forEach(writeBuilder::addDeleteItem);
        client.batchWriteItem(w -> w.addWriteBatch(writeBuilder.build()));
    }

    @Override
    public void deleteAll() {
        WriteBatch.Builder<T> writeBuilder = writeBuilderProvider.getIfAvailable();
        findAll().forEach(writeBuilder::addDeleteItem);
        client.batchWriteItem(w -> w.addWriteBatch(writeBuilder.build()));
    }
}
