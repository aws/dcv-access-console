// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.authorization.engines.entities;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AddUserToGroupsResponse {
    List<String> successfulGroups;
    List<String> unsuccessfulGroups;
}
