// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import handler.repositories.dto.RepositoryResponse;
import handler.repositories.dto.RepositoryRequest;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface PagingAndSortingCrudRepository<T, ID> extends CrudRepository<T, ID>, PagingAndSortingRepository<T, ID> {
    RepositoryResponse<T> findAll(RepositoryRequest request);
}