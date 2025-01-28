// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.utils;

import handler.model.Session;
import handler.model.Server;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.GetSessionScreenshotsUIRequestData;
import handler.model.SortToken;
import handler.exceptions.BadRequestException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SortTest {
    @InjectMocks
    private Sort<DescribeSessionsUIRequestData, Session> testSort;
    @InjectMocks
    private Sort<GetSessionScreenshotsUIRequestData, Session> testBadRequestTypeSort;
    private static List<Session> unsortedSessions;
    private static List<Session> sortedAscSessions;
    private static SortToken token;
    private static DescribeSessionsUIRequestData request;

    public SortTest() {
        Server server = new Server();
        Session session1 = new Session().name("session1");
        session1.setMaxConcurrentClients(1L);
        session1.setCreationTime(OffsetDateTime.parse("2023-07-10T00:00:00.001Z"));
        session1.setServer(server);
        Session session2 = new Session().name("session2");
        session2.setMaxConcurrentClients(2L);
        session2.setCreationTime(OffsetDateTime.parse("2023-07-10T00:00:00.002Z"));
        Session session3 = new Session();
        session3.setServer(server);
        Session session4 = session3;
        unsortedSessions = new ArrayList<>();
        unsortedSessions.add(session1);
        unsortedSessions.add(session4);
        unsortedSessions.add(session2);
        unsortedSessions.add(session3);

        sortedAscSessions = new ArrayList<>();
        sortedAscSessions.add(session3);
        sortedAscSessions.add(session4);
        sortedAscSessions.add(session1);
        sortedAscSessions.add(session2);

        token = new SortToken();

        request = new DescribeSessionsUIRequestData();
    }

    @Test
    public void testBadRequestException() {
        assertThrowsExactly(BadRequestException.class,
                () -> testSort.getSorted(null, unsortedSessions));

        assertThrowsExactly(BadRequestException.class,
                () -> testSort.getSorted(request.sortToken(token.operator(null)), unsortedSessions));

        assertThrowsExactly(BadRequestException.class,
                () -> testSort.getSorted(request.sortToken(token.operator(SortToken.OperatorEnum.ASC)), null));

        assertThrowsExactly(BadRequestException.class,
                () -> testSort.getSorted(request.sortToken(token.key(null)), unsortedSessions));

        token.setKey("Server");
        assertThrowsExactly(BadRequestException.class,
                () -> testSort.getSorted(request.sortToken(token), unsortedSessions));

        assertThrowsExactly(BadRequestException.class,
                () -> testBadRequestTypeSort.getSorted(new GetSessionScreenshotsUIRequestData(), null));
    }

    @Test
    public void testGetSortedSuccess() {
        List<Session> sortedDscSessions = (ArrayList<Session>) ((ArrayList<Session>) sortedAscSessions).clone();
        Collections.reverse(sortedDscSessions);

        token.setOperator(SortToken.OperatorEnum.ASC);
        token.setKey("Name");
        request.setSortToken(token);
        List<Session> sorted = testSort.getSorted(request, unsortedSessions);
        assertEquals(sortedAscSessions, sorted);

        token.setOperator(SortToken.OperatorEnum.DESC);
        request.setSortToken(token);
        sorted = testSort.getSorted(request, unsortedSessions);
        assertEquals(sortedDscSessions, sorted);

        token.setKey("MaxConcurrentClients");
        request.setSortToken(token);
        sorted = testSort.getSorted(request, unsortedSessions);
        assertEquals(sortedDscSessions, sorted);

        token.setKey("CreationTime");
        request.setSortToken(token);
        sorted = testSort.getSorted(request, unsortedSessions);
        assertEquals(sortedDscSessions, sorted);

        request.setSortToken(null);
        sorted = testSort.getSorted(request, unsortedSessions);
        assertEquals(unsortedSessions, sorted);
    }
}
