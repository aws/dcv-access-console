// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbFlatten;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

@Setter
@Entity
@DynamoDbBean
@EqualsAndHashCode
public class UserGroupUserMembership {
    @EmbeddedId
    @Getter(onMethod_=@DynamoDbFlatten)
    private UserGroupUser id;


    @ManyToOne
    @MapsId("userGroupId")
    @Getter(onMethod_=@DynamoDbIgnore)
    @JoinColumn(name = "userGroupId")
    private UserGroupEntity userGroup;

    @ManyToOne
    @MapsId("userId")
    @Getter(onMethod_=@DynamoDbIgnore)
    @JoinColumn(name = "userId")
    private UserEntity user;

    public UserGroupUserMembership() {}

    public UserGroupUserMembership(UserGroupEntity userGroup, UserEntity user) {
        UserGroupUser id = new UserGroupUser();
        id.setUserGroupId(userGroup.getUserGroupId());
        id.setUserId(user.getUserId());
        this.id = id;
        this.userGroup = userGroup;
        this.user = user;
    }
}
