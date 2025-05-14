// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.persistence;

import handler.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.OffsetDateTime;

@Entity
@DynamoDbBean
public class UserEntity extends User {
    public UserEntity() {
        super();
    }

    @Id
    @Override
    @DynamoDbPartitionKey
    public String getUserId() {
        return super.getUserId();
    }

    @Override
    public String getLoginUsername() {
        return super.getLoginUsername();
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    public String getRole() {
        return super.getRole();
    }

    @Override
    @Column(columnDefinition = "boolean")
    public Boolean getIsDisabled() {
        return super.getIsDisabled();
    }

    @Override
    public String getDisabledReason() {
        return super.getDisabledReason();
    }

    @Override
    @Column(columnDefinition = "boolean")
    public Boolean getIsImported() {
        return super.getIsImported();
    }

    @Override
    public OffsetDateTime getCreationTime() {
        return super.getCreationTime();
    }

    @Override
    public OffsetDateTime getLastModifiedTime() {
        return super.getLastModifiedTime();
    }

    @Override
    public OffsetDateTime getLastLoggedInTime() {
        return super.getLastLoggedInTime();
    }
}
