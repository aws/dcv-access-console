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
public class UserGroupUser implements Serializable {
    @Getter(onMethod_={@DynamoDbPartitionKey})
    private String userGroupId;

    @Getter(onMethod_={@DynamoDbSecondaryPartitionKey(indexNames = "userId"), @DynamoDbSortKey})
    private String userId;
}
