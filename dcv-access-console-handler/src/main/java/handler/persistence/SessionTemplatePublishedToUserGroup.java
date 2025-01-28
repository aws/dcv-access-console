// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.persistence;

import handler.model.SessionTemplate;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbFlatten;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

import java.time.OffsetDateTime;

@Setter
@Entity
@DynamoDbBean
public class SessionTemplatePublishedToUserGroup {
    @EmbeddedId
    @Getter(onMethod_=@DynamoDbFlatten)
    private SessionTemplateUserGroupId id;

    @ManyToOne
    @MapsId("sessionTemplateId")
    @Getter(onMethod_=@DynamoDbIgnore)
    @JoinColumn(name = "sessionTemplateId")
    private SessionTemplate sessionTemplate;

    @ManyToOne
    @MapsId("userGroupId")
    @JoinColumn(name = "userGroupId")
    @Getter(onMethod_=@DynamoDbIgnore)
    private UserGroupEntity userGroup;

    @Getter
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
    )
    private OffsetDateTime publishedTime;

    public SessionTemplatePublishedToUserGroup() {
    }

    public SessionTemplatePublishedToUserGroup(SessionTemplate sessionTemplate, UserGroupEntity userGroup) {
        SessionTemplateUserGroupId id = new SessionTemplateUserGroupId();
        id.setSessionTemplateId(sessionTemplate.getId());
        id.setUserGroupId(userGroup.getUserGroupId());
        this.id = id;
        this.sessionTemplate = sessionTemplate;
        this.userGroup = userGroup;
        this.publishedTime = OffsetDateTime.now();
    }
}