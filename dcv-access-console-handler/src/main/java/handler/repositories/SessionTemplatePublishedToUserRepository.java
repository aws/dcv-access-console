// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories;

import handler.persistence.SessionTemplatePublishedToUser;
import handler.persistence.SessionTemplateUserId;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface SessionTemplatePublishedToUserRepository extends PagingAndSortingCrudRepository<SessionTemplatePublishedToUser, SessionTemplateUserId> {
     List<SessionTemplatePublishedToUser> findBySessionTemplateId(String sessionTemplateId);
     List<SessionTemplatePublishedToUser> findByUserUserId(String userId);
}