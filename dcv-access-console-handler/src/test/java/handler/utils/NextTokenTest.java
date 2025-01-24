package handler.utils;

import handler.exceptions.BadRequestException;
import handler.model.SessionTemplate;
import handler.utils.NextToken;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NextTokenTest {
    @Test
    public void testFromWithMySqlParams() {
        int pageNumber = 2;
        int totalPages = 3;
        int pageOffset = 4;

        NextToken actualNextToken = NextToken.from(pageNumber, totalPages, pageOffset);
        assertEquals(actualNextToken.getDynamoDbStartKey(), Optional.empty());
        assertEquals(actualNextToken.getPageNumber(), OptionalInt.of(pageNumber));
        assertEquals(actualNextToken.getPageOffset(), OptionalInt.of(pageOffset));

        pageNumber = 3;

        NextToken nullNextToken = NextToken.from(pageNumber, totalPages, pageOffset);
        assertNull(nullNextToken);
    }

    @Test
    public void testFromWithDdbParams() {
        Map<String, AttributeValue> startKeyMap = Map.of("id", AttributeValue.fromS("fakePrimaryKey"));
        int pageOffset = 0;

        NextToken actualNextToken = NextToken.from(startKeyMap, SessionTemplate.class);
        assertEquals(actualNextToken.getDynamoDbStartKey(), Optional.of(startKeyMap));
        assertEquals(actualNextToken.getPageNumber(), OptionalInt.empty());
        assertEquals(actualNextToken.getPageOffset(), OptionalInt.of(pageOffset));

        NextToken nullNextToken = NextToken.from(null, SessionTemplate.class);
        assertNull(nullNextToken);
    }

    @Test
    public void testFromWithDdbParamsException() {
        Map<String, AttributeValue> startKeyMap = Map.of("id", AttributeValue.fromS("fakePrimaryKey"));

        UnsupportedOperationException thrown = assertThrows(
                UnsupportedOperationException.class,
                () -> NextToken.from(startKeyMap, AttributeValue.class),
                "Expected NextToken to throw UnsupportedOperationException, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Could not find primary key for class"));
    }

    @Test
    public void testDeserializeSuccess() {
        String serializedToken = "{\"page_number\":\"1\",\"dynamodb_start_key\":\"1d32a610-e564-45bb-9e51-0493d64c83ea\",\"page_offset\":\"0\"}";
        Map<String, AttributeValue> startKeyMap = Map.of("id", AttributeValue.fromS("1d32a610-e564-45bb-9e51-0493d64c83ea"));

        NextToken actualNextToken = NextToken.deserialize(serializedToken, SessionTemplate.class);
        assertEquals(actualNextToken.getDynamoDbStartKey(), Optional.of(startKeyMap));
        assertEquals(actualNextToken.getPageNumber(), OptionalInt.of(1));
        assertEquals(actualNextToken.getPageOffset(), OptionalInt.of(0));
    }

    @Test
    public void testDeserializeEmpty() {
        NextToken actualNextToken = NextToken.deserialize(null, SessionTemplate.class);
        assertEquals(actualNextToken.getDynamoDbStartKey(), Optional.empty());
        assertEquals(actualNextToken.getPageNumber(), OptionalInt.of(0));
        assertEquals(actualNextToken.getPageOffset(), OptionalInt.of(0));
    }

    @Test
    public void testDeserializeException() {
        String badSerializedToken = "abcdefg";

        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> NextToken.deserialize(badSerializedToken, SessionTemplate.class),
                "Expected NextToken to throw BadRequestException, but it didn't"
        );
    }

    @Test
    public void testSerializeSuccessWithStartKey() {
        Map<String, AttributeValue> startKeyMap = Map.of("id", AttributeValue.fromS("fakePrimaryKey"));
        NextToken nextToken = NextToken.builder()
                .dynamoDbStartKey(Optional.of(startKeyMap))
                .pageNumber(OptionalInt.of(1))
                .pageOffset(OptionalInt.of(0))
                .build();

        String serializedNextToken = NextToken.serialize(nextToken, SessionTemplate.class);

        assertTrue(serializedNextToken.contains("\"dynamodb_start_key\":\"fakePrimaryKey\""));
        assertTrue(serializedNextToken.contains("\"page_number\":\"1\""));
        assertTrue(serializedNextToken.contains("\"page_offset\":\"0\""));
    }

    @Test
    public void testSerializeSuccessWithoutStartKey() {
        NextToken nextToken = NextToken.builder()
                .dynamoDbStartKey(Optional.empty())
                .pageNumber(OptionalInt.of(1))
                .pageOffset(OptionalInt.of(0))
                .build();

        String serializedNextToken = NextToken.serialize(nextToken, SessionTemplate.class);

        assertTrue(serializedNextToken.contains("\"dynamodb_start_key\":null"));
        assertTrue(serializedNextToken.contains("\"page_number\":\"1\""));
        assertTrue(serializedNextToken.contains("\"page_offset\":\"0\""));
    }

    @Test
    public void testSerializeNull() {
        assertNull(NextToken.serialize(null, SessionTemplate.class));
    }
}