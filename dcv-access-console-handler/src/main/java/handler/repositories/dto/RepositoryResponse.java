package handler.repositories.dto;

import handler.utils.NextToken;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Builder
@Getter
@Setter
public class RepositoryResponse<T> {
    private List<T> items;
    private NextToken nextToken;
}
