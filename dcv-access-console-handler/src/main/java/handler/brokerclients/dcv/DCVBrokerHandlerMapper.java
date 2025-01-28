// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.brokerclients.dcv;

import broker.model.DeleteSessionsResponse;
import broker.model.DescribeServersResponse;
import broker.model.DescribeSessionsResponse;
import broker.model.GetSessionConnectionDataResponse;
import broker.model.GetSessionScreenshotsResponse;
import handler.model.DeleteSessionsUIResponse;
import handler.model.DescribeServersUIResponse;
import handler.model.DescribeSessionsUIResponse;
import handler.model.GetSessionConnectionDataUIResponse;
import handler.model.GetSessionScreenshotsUIResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DCVBrokerHandlerMapper {
    DescribeSessionsUIResponse mapToDescribeSessionsUIResponse(DescribeSessionsResponse response);

    GetSessionScreenshotsUIResponse mapToGetSessionScreenshotsUIResponse(GetSessionScreenshotsResponse response);

    List<broker.model.KeyValuePair> mapToBrokerKeyValuePairs(List<handler.model.KeyValuePair> keyValuePairs);

    DescribeServersUIResponse mapToDescribeServersUIResponse(DescribeServersResponse response);

    handler.model.CreateSessionRequestData mapToHandlerCreateSessionRequestData(broker.model.CreateSessionRequestData createSessionRequestData);

    handler.model.SessionWithPermissions mapToHandlerSession(broker.model.Session session);

    GetSessionConnectionDataUIResponse mapToGetSessionConnectionDataUIResponse(GetSessionConnectionDataResponse response);

    DeleteSessionsUIResponse mapToDeleteSessionsDataUIResponse(DeleteSessionsResponse response);
}