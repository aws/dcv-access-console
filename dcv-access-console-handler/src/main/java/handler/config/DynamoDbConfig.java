// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.config;

import handler.model.SessionTemplate;

import handler.persistence.SessionTemplatePublishedToUser;
import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.UserEntity;
import handler.persistence.UserGroupEntity;
import handler.persistence.UserGroupUserMembership;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.context.annotation.RequestScope;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Slf4j
@ConditionalOnProperty(name = "persistence-db", havingValue = "dynamodb")
@Configuration
public class DynamoDbConfig {
    @Value("${table-name-prefix:dcv_sm_ui_}")
    private String prefix;

    private void createTable(String tableName, DynamoDbTable table, DynamoDbClient dynamoDbClient) {
        try {
            table.createTable();
            try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build()) {
                ResponseOrException<DescribeTableResponse> response = waiter
                        .waitUntilTableExists(builder -> builder.tableName(tableName).build())
                        .matched();
                response.response().orElseThrow(
                        () -> new RuntimeException(tableName + " DynamoDB table was not created."));
                if(response.exception().isEmpty()) {
                    log.info(tableName + " DynamoDB table created.");
                }
            }
        } catch (ResourceInUseException e) {
            log.info("Loaded existing {} DynamoDB table.", tableName);
        } catch (Exception e) {
            log.error("Error creating {} DynamoDB table", tableName, e);
        }
    }

    @Bean
    public DynamoDbClient provideClient(@Value("${dynamodb-region:#{null}}") String region) {
        DynamoDbClientBuilder dynamoDbClientBuilder = DynamoDbClient.builder();
        if(region != null) {
            dynamoDbClientBuilder = dynamoDbClientBuilder.region(Region.of(region));
        }
        return dynamoDbClientBuilder.build();
    }

    @Bean
    public DynamoDbEnhancedClient provideEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    }

    @Bean
    public DynamoDbTable<SessionTemplate> provideSessionTemplateTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbClient dynamoDbClient) {
        String tableName = prefix+"SessionTemplate";
        DynamoDbTable<SessionTemplate> table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(SessionTemplate.class));
        createTable(tableName, table, dynamoDbClient);
        return table;
    }

    @Bean
    public DynamoDbTable<UserEntity> provideUserTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbClient dynamoDbClient) {
        String tableName = prefix+"User";
        DynamoDbTable<UserEntity> table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(UserEntity.class));
        createTable(tableName, table, dynamoDbClient);
        return table;
    }

    @Bean
    public DynamoDbTable<SessionTemplatePublishedToUser> provideSessionTemplatePublishedToUserTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbClient dynamoDbClient) {
        String tableName = prefix+"SessionTemplatePublishedToUser";
        DynamoDbTable<SessionTemplatePublishedToUser> table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(SessionTemplatePublishedToUser.class));
        createTable(tableName, table, dynamoDbClient);
        return table;
    }

    @Bean
    public DynamoDbTable<UserGroupEntity> provideUserGroupTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbClient dynamoDbClient) {
        String tableName = prefix+"UserGroup";
        DynamoDbTable<UserGroupEntity> table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(UserGroupEntity.class));
        createTable(tableName, table, dynamoDbClient);
        return table;
    }

    @Bean
    public DynamoDbTable<SessionTemplatePublishedToUserGroup> provideSessionTemplatePublishedToUserGroupTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbClient dynamoDbClient) {
        String tableName = prefix+"SessionTemplatePublishedToUserGroup";
        DynamoDbTable<SessionTemplatePublishedToUserGroup> table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(SessionTemplatePublishedToUserGroup.class));
        createTable(tableName, table, dynamoDbClient);
        return table;
    }

    @Bean
    public DynamoDbTable<UserGroupUserMembership> provideUserGroupUserMembershipTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbClient dynamoDbClient) {
        String tableName = prefix+"UserGroupUserMembership";
        DynamoDbTable<UserGroupUserMembership> table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromClass(UserGroupUserMembership.class));
        createTable(tableName, table, dynamoDbClient);
        return table;
    }

    @Bean
    @RequestScope
    public WriteBatch.Builder<SessionTemplate> provideSessionTemplateWriteBuilder(DynamoDbTable<SessionTemplate> table) {
        return WriteBatch.builder(SessionTemplate.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public WriteBatch.Builder<UserEntity> provideUserWriteBuilder(DynamoDbTable<UserEntity> table) {
        return WriteBatch.builder(UserEntity.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public WriteBatch.Builder<SessionTemplatePublishedToUser> provideSessionTemplatePublishedToUserWriteBuilder(DynamoDbTable<SessionTemplatePublishedToUser> table) {
        return WriteBatch.builder(SessionTemplatePublishedToUser.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public WriteBatch.Builder<UserGroupEntity> provideUserGroupWriteBuilder(DynamoDbTable<UserGroupEntity> table) {
        return WriteBatch.builder(UserGroupEntity.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public WriteBatch.Builder<SessionTemplatePublishedToUserGroup> provideSessionTemplatePublishedToUserGroupWriteBuilder(DynamoDbTable<SessionTemplatePublishedToUserGroup> table) {
        return WriteBatch.builder(SessionTemplatePublishedToUserGroup.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public WriteBatch.Builder<UserGroupUserMembership> provideUserGroupUserMembershipWriteBuilder(DynamoDbTable<UserGroupUserMembership> table) {
        return WriteBatch.builder(UserGroupUserMembership.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public ReadBatch.Builder<SessionTemplate> provideSessionTemplateReadBuilder(DynamoDbTable<SessionTemplate> table) {
        return ReadBatch.builder(SessionTemplate.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public ReadBatch.Builder<UserEntity> provideUserReadBuilder(DynamoDbTable<UserEntity> table) {
        return ReadBatch.builder(UserEntity.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public ReadBatch.Builder<SessionTemplatePublishedToUser> provideSessionTemplatePublishedToUserReadBuilder(DynamoDbTable<SessionTemplatePublishedToUser> table) {
        return ReadBatch.builder(SessionTemplatePublishedToUser.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public ReadBatch.Builder<UserGroupEntity> provideUserGroupReadBuilder(DynamoDbTable<UserGroupEntity> table) {
        return ReadBatch.builder(UserGroupEntity.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public ReadBatch.Builder<SessionTemplatePublishedToUserGroup> provideSessionTemplatePublishedToUserGroupReadBuilder(DynamoDbTable<SessionTemplatePublishedToUserGroup> table) {
        return ReadBatch.builder(SessionTemplatePublishedToUserGroup.class).mappedTableResource(table);
    }

    @Bean
    @RequestScope
    public ReadBatch.Builder<UserGroupUserMembership> provideUserGroupUserMembershipReadBuilder(DynamoDbTable<UserGroupUserMembership> table) {
        return ReadBatch.builder(UserGroupUserMembership.class).mappedTableResource(table);
    }
}