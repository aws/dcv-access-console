// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.persistence;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.io.Serializable;

@Setter
@Embeddable
@DynamoDbBean
@EqualsAndHashCode
public class SessionTemplateUserGroupId implements Serializable {
    @Getter(onMethod_={@DynamoDbPartitionKey})
    private String sessionTemplateId;

    @Getter(onMethod_={@DynamoDbSecondaryPartitionKey(indexNames = "userGroupId"), @DynamoDbSortKey})
    private String userGroupId;
}
