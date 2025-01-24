package handler.repositories.mysql;

import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.dto.RepositoryResponse;
import handler.repositories.dto.RepositoryRequest;
import handler.utils.NextToken;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "persistence-db", havingValue = "mysql")
@NoRepositoryBean
public interface MySqlRepository<T, ID> extends PagingAndSortingCrudRepository<T, ID> {
    default RepositoryResponse<T> findAll(RepositoryRequest request) {
        PageRequest pageRequest = PageRequest.ofSize(request.getMaxResults())
                .withPage(request.getNextToken().getPageNumber().getAsInt())
                .withSort(request.getSort());
        Page<T> page = findAll(pageRequest);

        List<T> items = page.getContent().subList(request.getNextToken().getPageOffset().getAsInt(), page.getContent().size());
        NextToken newNextToken = NextToken.from(request.getNextToken().getPageNumber().getAsInt() + 1, page.getTotalPages(), 0);

        return RepositoryResponse.<T>builder().items(items).nextToken(newNextToken).build();
    }
}
