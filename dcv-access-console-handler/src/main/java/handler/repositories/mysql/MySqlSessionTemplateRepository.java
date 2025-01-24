package handler.repositories.mysql;

import handler.model.SessionTemplate;
import handler.repositories.PagingAndSortingCrudRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "persistence-db", havingValue = "mysql")
@Repository
public interface MySqlSessionTemplateRepository extends MySqlRepository<SessionTemplate, String> {
}