// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.brokerclients;

import handler.model.CreateSessionTemplateRequestData;
import handler.model.CreateSessionUIRequestData;
import handler.model.CreateSessionsUIResponse;
import handler.model.DeleteSessionUIRequestData;
import handler.model.DeleteSessionsUIResponse;
import handler.model.DescribeServersUIRequestData;
import handler.model.DescribeServersUIResponse;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.DescribeSessionsUIResponse;
import handler.model.GetSessionConnectionDataUIResponse;
import handler.model.GetSessionScreenshotsUIRequestData;
import handler.model.GetSessionScreenshotsUIResponse;
import handler.model.SessionTemplate;
import org.javatuples.Pair;

import java.util.List;

public abstract class BrokerClient {
    public abstract DescribeSessionsUIResponse describeSessions(DescribeSessionsUIRequestData request) throws Exception;
    
    public abstract GetSessionScreenshotsUIResponse getSessionScreenshots(GetSessionScreenshotsUIRequestData request) throws Exception;

    public abstract DescribeServersUIResponse describeServers(DescribeServersUIRequestData request) throws Exception;

    public abstract void validateSessionTemplate(CreateSessionTemplateRequestData request, boolean ignoreExisting);

    public abstract CreateSessionsUIResponse createSessions(List<Pair<CreateSessionUIRequestData, SessionTemplate>> requests);

    public abstract GetSessionConnectionDataUIResponse getSessionConnectionData(String sessionId, String username);

    public abstract DeleteSessionsUIResponse deleteSessions(List<DeleteSessionUIRequestData> deleteSessionsUIRequestData);
}