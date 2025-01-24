package handler.repositories;

import handler.persistence.SessionTemplateUserGroupId;
import handler.persistence.SessionTemplatePublishedToUserGroup;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface SessionTemplatePublishedToUserGroupRepository extends CrudRepository<SessionTemplatePublishedToUserGroup, SessionTemplateUserGroupId> {
     List<SessionTemplatePublishedToUserGroup> findBySessionTemplateId(String sessionTemplateId);

     List<SessionTemplatePublishedToUserGroup> findByUserGroupUserGroupId(String userGroupId);

     RepositoryResponse<SessionTemplatePublishedToUserGroup> findByUserGroupUserGroupId(String userGroupId, RepositoryRequest request);
}