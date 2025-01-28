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
public class SessionTemplatePublishedToUser {
    @EmbeddedId
    @Getter(onMethod_=@DynamoDbFlatten)
    private SessionTemplateUserId id;

    @ManyToOne
    @MapsId("sessionTemplateId")
    @Getter(onMethod_=@DynamoDbIgnore)
    @JoinColumn(name = "sessionTemplateId")
    private SessionTemplate sessionTemplate;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "userId")
    @Getter(onMethod_=@DynamoDbIgnore)
    private UserEntity user;

    @Getter
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
    )
    private OffsetDateTime publishedTime;

    public SessionTemplatePublishedToUser() {
    }

    public SessionTemplatePublishedToUser(SessionTemplate sessionTemplate, UserEntity user) {
        SessionTemplateUserId id = new SessionTemplateUserId();
        id.setSessionTemplateId(sessionTemplate.getId());
        id.setUserId(user.getUserId());
        this.id = id;
        this.sessionTemplate = sessionTemplate;
        this.user = user;
        this.publishedTime = OffsetDateTime.now();
    }
}