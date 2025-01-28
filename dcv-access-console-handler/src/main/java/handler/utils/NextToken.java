// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.utils;

import handler.exceptions.BadRequestException;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeUsersRequestData;
import handler.model.SessionTemplate;
import handler.persistence.UserEntity;
import handler.persistence.UserGroupEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalInt;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
@Setter
public class NextToken {
    Optional<Map<String, AttributeValue>> dynamoDbStartKey;
    OptionalInt pageNumber;
    OptionalInt pageOffset;

    public static final String DYNAMODB_START_KEY = "dynamodb_start_key";
    public static final String PAGE_NUMBER = "page_number";
    public static final String PAGE_OFFSET = "page_offset";

    private static final Map<Class, String> DDB_PRIMARY_KEYS = Map.ofEntries(
            Map.entry(UserEntity.class, "userId"),
            Map.entry(UserGroupEntity.class, "userGroupId"),
            Map.entry(SessionTemplate.class, "id")
    );

    public static NextToken from(int pageNumber, int totalPages, int pageOffset) {
        if (pageNumber >= totalPages) {
            return null;
        } else {
            return builder()
                    .dynamoDbStartKey(Optional.empty())
                    .pageNumber(OptionalInt.of(pageNumber))
                    .pageOffset(OptionalInt.of(pageOffset))
                    .build();
        }
    }

    public static NextToken from(Map<String, AttributeValue> startKeyMap, Class c) {
        if (!DDB_PRIMARY_KEYS.containsKey(c)) {
            throw new UnsupportedOperationException("Could not find primary key for class" + c);
        } else if (startKeyMap == null) {
            return null;
        } else {
            String startKeyString = startKeyMap.get(DDB_PRIMARY_KEYS.get(c)).s().toString();
            return builder()
                    .dynamoDbStartKey(Optional.of(startKeyMap))
                    .pageNumber(OptionalInt.empty())
                    .pageOffset(OptionalInt.of(0))
                    .build();
        }
    }

    public static NextToken deserialize(String tokenString, Class c) {
        if (StringUtils.isEmpty(tokenString)) {
            return builder().dynamoDbStartKey(Optional.empty()).pageNumber(OptionalInt.of(0)).pageOffset(OptionalInt.of(0)).build();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> stringMap = objectMapper.readValue(tokenString, Map.class);
            Optional<Map<String, AttributeValue>> dynamoDbStartKey;
            OptionalInt pageNumber;
            OptionalInt pageOffset;

            if (DDB_PRIMARY_KEYS.containsKey(c) && stringMap.containsKey(DYNAMODB_START_KEY)) {
                dynamoDbStartKey = Optional.of(Map.of(DDB_PRIMARY_KEYS.get(c), AttributeValue.fromS(stringMap.get(DYNAMODB_START_KEY))));
            } else {
                dynamoDbStartKey = Optional.empty();
            }

            if (stringMap.containsKey(PAGE_NUMBER)) {
                pageNumber = OptionalInt.of(Integer.valueOf(stringMap.get(PAGE_NUMBER)));
            } else {
                pageNumber = OptionalInt.empty();
            }

            if (stringMap.containsKey(PAGE_OFFSET)) {
                pageOffset = OptionalInt.of(Integer.valueOf(stringMap.get(PAGE_OFFSET)));
            } else {
                pageOffset = OptionalInt.empty();
            }

            return builder().dynamoDbStartKey(dynamoDbStartKey).pageNumber(pageNumber).pageOffset(pageOffset).build();
        } catch (IOException e) {
            throw new BadRequestException(e);
        }
    }

    public static String serialize(NextToken token, Class c) {
        if (token == null) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String dynamoDbStartKey;

            if (token.getDynamoDbStartKey().isPresent() && ((Map<String, AttributeValue>)token.getDynamoDbStartKey().get()).get(DDB_PRIMARY_KEYS.get(c)).s() != null) {
                dynamoDbStartKey = ((Map<String, AttributeValue>)token.getDynamoDbStartKey().get()).get(DDB_PRIMARY_KEYS.get(c)).s().toString();
            } else {
                dynamoDbStartKey = null;
            }

            HashMap<String, String> tokenMap = new HashMap<>();
            tokenMap.put(DYNAMODB_START_KEY, dynamoDbStartKey);
            tokenMap.put(PAGE_NUMBER, Integer.toString(token.getPageNumber().orElse(1)));
            tokenMap.put(PAGE_OFFSET, Integer.toString(token.getPageOffset().orElse(0)));
            return objectMapper.writeValueAsString(tokenMap);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize nextToken", e);
        }
    }
}