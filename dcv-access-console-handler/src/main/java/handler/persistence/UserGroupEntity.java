// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.persistence;

import handler.model.User;
import handler.model.UserGroup;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@DynamoDbBean
public class UserGroupEntity extends UserGroup {

    public UserGroupEntity() {
        super();
    }

    @Id
    @Override
    @DynamoDbPartitionKey
    public String getUserGroupId() {
        return super.getUserGroupId();
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName();
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
}
