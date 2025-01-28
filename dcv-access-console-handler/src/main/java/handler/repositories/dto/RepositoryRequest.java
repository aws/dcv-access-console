// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.dto;

import handler.utils.NextToken;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Builder
@Getter
@Setter
public class RepositoryRequest {
    private NextToken nextToken;
    private int maxResults;
    private Sort sort;
    private Class<?> clazz;
}
