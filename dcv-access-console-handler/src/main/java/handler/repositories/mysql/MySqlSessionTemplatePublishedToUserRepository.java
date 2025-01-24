package handler.repositories.mysql;

import handler.persistence.SessionTemplatePublishedToUser;
import handler.persistence.SessionTemplateUserId;
import handler.repositories.SessionTemplatePublishedToUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

@ConditionalOnProperty(name = "persistence-db", havingValue = "mysql")
@Repository
public interface MySqlSessionTemplatePublishedToUserRepository extends SessionTemplatePublishedToUserRepository, MySqlRepository<SessionTemplatePublishedToUser, SessionTemplateUserId> {
    @Override
    List<SessionTemplatePublishedToUser> findBySessionTemplateId(String sessionTemplateId);

    @Override
    List<SessionTemplatePublishedToUser> findByUserUserId(String userId);
}
