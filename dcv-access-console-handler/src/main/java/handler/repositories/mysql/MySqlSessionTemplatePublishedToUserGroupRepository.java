// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.repositories.mysql;

import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.persistence.SessionTemplateUserGroupId;
import handler.repositories.SessionTemplatePublishedToUserGroupRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import handler.utils.NextToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@ConditionalOnProperty(name = "persistence-db", havingValue = "mysql")
@Repository
public interface MySqlSessionTemplatePublishedToUserGroupRepository extends SessionTemplatePublishedToUserGroupRepository, MySqlRepository<SessionTemplatePublishedToUserGroup, SessionTemplateUserGroupId>, JpaRepository<SessionTemplatePublishedToUserGroup, SessionTemplateUserGroupId> {
    @Override
    List<SessionTemplatePublishedToUserGroup> findBySessionTemplateId(String sessionTemplateId);

    @Override
    List<SessionTemplatePublishedToUserGroup> findByUserGroupUserGroupId(String userGroupId);

    Page<SessionTemplatePublishedToUserGroup> findByUserGroupUserGroupId(String userGroupId, Pageable pageable);

    @Override
    default RepositoryResponse<SessionTemplatePublishedToUserGroup> findByUserGroupUserGroupId(String userGroupId, RepositoryRequest request) {
        PageRequest pageRequest = PageRequest.ofSize(request.getMaxResults())
                .withPage(request.getNextToken().getPageNumber().getAsInt())
                .withSort(request.getSort());
        Page<SessionTemplatePublishedToUserGroup> page = findByUserGroupUserGroupId(userGroupId, pageRequest);

        List<SessionTemplatePublishedToUserGroup> items = page.getContent().subList(request.getNextToken().getPageOffset().getAsInt(), page.getContent().size());
        NextToken newNextToken = NextToken.from(request.getNextToken().getPageNumber().getAsInt() + 1, page.getTotalPages(), 0);

        return RepositoryResponse.<SessionTemplatePublishedToUserGroup>builder().items(items).nextToken(newNextToken).build();
    }
}
